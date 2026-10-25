package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.example.data.model.GeoPoint
import com.example.data.model.LiveMetrics
import com.example.data.model.TrackingStatus
import com.example.util.GpsMetricsCalculator
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TrackingManager private constructor(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    val bleHeartRateManager: BleHeartRateManager = BleHeartRateManager.getInstance(context)

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null

    private val _status = MutableStateFlow(TrackingStatus.IDLE)
    val status: StateFlow<TrackingStatus> = _status.asStateFlow()

    private val _metrics = MutableStateFlow(LiveMetrics())
    val metrics: StateFlow<LiveMetrics> = _metrics.asStateFlow()

    private val _gpsAccuracyThreshold = MutableStateFlow(35.0f)

    private var startTimeMillis: Long = 0L
    private var lastRecordedPoint: GeoPoint? = null
    private var userWeightKg: Float = 70.0f
    private var maxRecordedSpeedMps: Double = 0.0

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            handleNewLocation(location)
        }
    }

    fun setWeightKg(weight: Float) {
        userWeightKg = weight
    }

    fun setAccuracyThreshold(threshold: Float) {
        _gpsAccuracyThreshold.value = threshold
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        if (_status.value == TrackingStatus.TRACKING) return

        startTimeMillis = System.currentTimeMillis()
        lastRecordedPoint = null
        maxRecordedSpeedMps = 0.0
        bleHeartRateManager.resetStats()

        _metrics.value = LiveMetrics()
        _status.value = TrackingStatus.ACQUIRING

        startTimer()
        requestLocationUpdates()
    }

    fun pauseTracking() {
        if (_status.value != TrackingStatus.TRACKING && _status.value != TrackingStatus.ACQUIRING) return
        _status.value = TrackingStatus.PAUSED
        timerJob?.cancel()
    }

    fun resumeTracking() {
        if (_status.value != TrackingStatus.PAUSED) return
        _status.value = TrackingStatus.TRACKING
        startTimer()
    }

    fun stopTracking(): LiveMetrics {
        val curBpm = bleHeartRateManager.currentHeartRate.value
        val avgBpm = if (bleHeartRateManager.getAverageHeartRate() > 0) bleHeartRateManager.getAverageHeartRate() else null
        val maxBpm = if (bleHeartRateManager.getMaxHeartRate() > 0) bleHeartRateManager.getMaxHeartRate() else null
        val hrZone = bleHeartRateManager.heartRateZone.value

        val finalMetrics = _metrics.value.copy(
            currentHeartRateBpm = curBpm,
            avgHeartRateBpm = avgBpm,
            maxHeartRateBpm = maxBpm,
            hrZone = hrZone
        )
        _status.value = TrackingStatus.STOPPED
        timerJob?.cancel()
        removeLocationUpdates()
        return finalMetrics
    }

    fun reset() {
        timerJob?.cancel()
        removeLocationUpdates()
        bleHeartRateManager.resetStats()
        _status.value = TrackingStatus.IDLE
        _metrics.value = LiveMetrics()
        lastRecordedPoint = null
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                delay(1000L)
                if (_status.value == TrackingStatus.TRACKING) {
                    val current = _metrics.value
                    val newDuration = current.durationSeconds + 1
                    val newCalories = GpsMetricsCalculator.calculateCalories(
                        newDuration,
                        current.distanceMeters,
                        userWeightKg
                    )
                    val newAvgPace = GpsMetricsCalculator.calculateAvgPaceSecondsPerKm(
                        current.distanceMeters,
                        newDuration
                    )
                    val curBpm = bleHeartRateManager.currentHeartRate.value
                    val avgBpm = if (bleHeartRateManager.getAverageHeartRate() > 0) bleHeartRateManager.getAverageHeartRate() else null
                    val maxBpm = if (bleHeartRateManager.getMaxHeartRate() > 0) bleHeartRateManager.getMaxHeartRate() else null
                    val hrZone = bleHeartRateManager.heartRateZone.value

                    _metrics.value = current.copy(
                        durationSeconds = newDuration,
                        calories = newCalories,
                        avgPaceSecondsPerKm = newAvgPace,
                        currentHeartRateBpm = curBpm,
                        avgHeartRateBpm = avgBpm,
                        maxHeartRateBpm = maxBpm,
                        hrZone = hrZone
                    )
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000L
        )
            .setMinUpdateIntervalMillis(500L)
            .setMinUpdateDistanceMeters(1.0f)
            .setWaitForAccurateLocation(true)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e("TrackingManager", "Location permission missing", e)
        }
    }

    private fun removeLocationUpdates() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            Log.e("TrackingManager", "Failed to remove location updates", e)
        }
    }

    private fun handleNewLocation(location: Location) {
        val geoPoint = GeoPoint(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude,
            accuracy = location.accuracy,
            speed = location.speed,
            timestampMillis = location.time
        )

        // Update GPS accuracy and current location even if filtered out of distance
        val current = _metrics.value

        if (_status.value == TrackingStatus.ACQUIRING) {
            if (location.accuracy <= _gpsAccuracyThreshold.value) {
                _status.value = TrackingStatus.TRACKING
                lastRecordedPoint = geoPoint
                _metrics.value = current.copy(
                    gpsAccuracyMeters = location.accuracy,
                    currentLocation = geoPoint,
                    routePoints = listOf(geoPoint)
                )
                return
            } else {
                _metrics.value = current.copy(
                    gpsAccuracyMeters = location.accuracy,
                    currentLocation = geoPoint
                )
                return
            }
        }

        if (_status.value != TrackingStatus.TRACKING) {
            _metrics.value = current.copy(
                gpsAccuracyMeters = location.accuracy,
                currentLocation = geoPoint
            )
            return
        }

        val prev = lastRecordedPoint
        if (prev == null) {
            lastRecordedPoint = geoPoint
            _metrics.value = current.copy(
                gpsAccuracyMeters = location.accuracy,
                currentLocation = geoPoint,
                routePoints = current.routePoints + geoPoint
            )
            return
        }

        val isValidMovement = GpsMetricsCalculator.isReliableMovement(
            prev = prev,
            curr = geoPoint,
            maxAccuracyMeters = _gpsAccuracyThreshold.value
        )

        if (isValidMovement) {
            val segmentDist = GpsMetricsCalculator.calculateHaversineDistance(
                prev.latitude, prev.longitude,
                geoPoint.latitude, geoPoint.longitude
            )
            val newDistance = current.distanceMeters + segmentDist
            val newSteps = GpsMetricsCalculator.calculateSteps(newDistance)
            val newCalories = GpsMetricsCalculator.calculateCalories(
                current.durationSeconds,
                newDistance,
                userWeightKg
            )
            val newAvgPace = GpsMetricsCalculator.calculateAvgPaceSecondsPerKm(
                newDistance,
                current.durationSeconds
            )

            val currentPace = if (location.speed > 0.5f) {
                1000.0 / location.speed
            } else {
                newAvgPace
            }

            if (location.speed > maxRecordedSpeedMps) {
                maxRecordedSpeedMps = location.speed.toDouble()
            }

            lastRecordedPoint = geoPoint

            _metrics.value = current.copy(
                distanceMeters = newDistance,
                steps = newSteps,
                calories = newCalories,
                avgPaceSecondsPerKm = newAvgPace,
                currentPaceSecondsPerKm = currentPace,
                gpsAccuracyMeters = location.accuracy,
                currentLocation = geoPoint,
                routePoints = current.routePoints + geoPoint
            )
        } else {
            // Still update the live position and accuracy badge
            _metrics.value = current.copy(
                gpsAccuracyMeters = location.accuracy,
                currentLocation = geoPoint
            )
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: TrackingManager? = null

        fun getInstance(context: Context): TrackingManager {
            return INSTANCE ?: synchronized(this) {
                val instance = TrackingManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
