package com.example.util

import com.example.data.model.GeoPoint
import com.example.data.model.UnitSystem
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GpsMetricsCalculator {

    private const val EARTH_RADIUS_METERS = 6371000.0
    private const val AVERAGE_STRIDE_METERS = 0.78

    fun calculateHaversineDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(rLat1) * cos(rLat2) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    fun isReliableMovement(
        prev: GeoPoint,
        curr: GeoPoint,
        maxAccuracyMeters: Float = 35f,
        minMovementMeters: Double = 2.0,
        maxSpeedMps: Double = 7.0 // ~25 km/h walking cap
    ): Boolean {
        // Discard low accuracy fixes
        if (curr.accuracy > maxAccuracyMeters) return false

        val dist = calculateHaversineDistance(
            prev.latitude, prev.longitude,
            curr.latitude, curr.longitude
        )

        // Ignore micro jitter while stationary
        if (dist < minMovementMeters) return false

        val timeDeltaSeconds = (curr.timestampMillis - prev.timestampMillis) / 1000.0
        if (timeDeltaSeconds > 0) {
            val impliedSpeed = dist / timeDeltaSeconds
            if (impliedSpeed > maxSpeedMps) return false
        }

        return true
    }

    fun calculateCalories(
        durationSeconds: Long,
        distanceMeters: Double,
        weightKg: Float
    ): Int {
        if (durationSeconds <= 0 || distanceMeters <= 0.0) return 0
        val speedMps = distanceMeters / durationSeconds
        val met = when {
            speedMps < 1.0 -> 2.8 // Slow stroll
            speedMps < 1.4 -> 3.5 // Moderate walking
            speedMps < 1.8 -> 4.3 // Brisk walking
            else -> 5.0          // Very brisk / power walking
        }
        val durationMinutes = durationSeconds / 60.0
        val calories = (met * 3.5 * weightKg / 200.0) * durationMinutes
        return calories.toInt().coerceAtLeast(1)
    }

    fun calculateSteps(distanceMeters: Double): Int {
        if (distanceMeters <= 0.0) return 0
        return (distanceMeters / AVERAGE_STRIDE_METERS).toInt()
    }

    fun calculateAvgPaceSecondsPerKm(
        distanceMeters: Double,
        durationSeconds: Long
    ): Double {
        if (distanceMeters < 10.0 || durationSeconds <= 0) return 0.0
        val km = distanceMeters / 1000.0
        return durationSeconds / km
    }

    fun formatDuration(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    fun formatPace(
        paceSecondsPerKm: Double,
        unitSystem: UnitSystem = UnitSystem.METRIC
    ): String {
        if (paceSecondsPerKm <= 0 || paceSecondsPerKm > 3600) return "--'--\""
        val adjustedPace = if (unitSystem == UnitSystem.IMPERIAL) {
            paceSecondsPerKm * 1.60934
        } else {
            paceSecondsPerKm
        }
        val minutes = (adjustedPace / 60).toInt()
        val seconds = (adjustedPace % 60).toInt()
        val unitLabel = if (unitSystem == UnitSystem.IMPERIAL) "/mi" else "/km"
        return String.format(Locale.US, "%d'%02d\" %s", minutes, seconds, unitLabel)
    }

    fun formatDistance(
        distanceMeters: Double,
        unitSystem: UnitSystem = UnitSystem.METRIC
    ): String {
        return if (unitSystem == UnitSystem.IMPERIAL) {
            val miles = distanceMeters * 0.000621371
            String.format(Locale.US, "%.2f mi", miles)
        } else {
            val km = distanceMeters / 1000.0
            String.format(Locale.US, "%.2f km", km)
        }
    }

    fun formatDistanceValueOnly(
        distanceMeters: Double,
        unitSystem: UnitSystem = UnitSystem.METRIC
    ): String {
        return if (unitSystem == UnitSystem.IMPERIAL) {
            val miles = distanceMeters * 0.000621371
            String.format(Locale.US, "%.2f", miles)
        } else {
            val km = distanceMeters / 1000.0
            String.format(Locale.US, "%.2f", km)
        }
    }

    fun getDistanceUnit(unitSystem: UnitSystem): String {
        return if (unitSystem == UnitSystem.IMPERIAL) "mi" else "km"
    }

    fun getPaceUnit(unitSystem: UnitSystem): String {
        return if (unitSystem == UnitSystem.IMPERIAL) "min/mi" else "min/km"
    }
}
