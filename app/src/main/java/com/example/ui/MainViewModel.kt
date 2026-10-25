package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.WalkEntity
import com.example.data.model.LiveMetrics
import com.example.data.model.TrackingStatus
import com.example.data.model.UnitSystem
import com.example.data.preferences.UserPreferencesRepository
import com.example.service.TrackingManager
import com.example.service.TrackingService
import com.example.util.GpsMetricsCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

sealed class Screen {
    object Splash : Screen()
    object Home : Screen()
    object LiveWorkout : Screen()
    data class WorkoutSummary(val walkEntity: WalkEntity) : Screen()
    object History : Screen()
    object Statistics : Screen()
    object Settings : Screen()
}

data class PersonalRecords(
    val longestWalkMeters: Double = 0.0,
    val fastestPaceSecPerKm: Double = 0.0,
    val longestDurationSeconds: Long = 0L,
    val totalDistanceMeters: Double = 0.0,
    val totalWalksCount: Int = 0,
    val totalCalories: Int = 0,
    val totalSteps: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val walkDao = db.walkDao()
    private val prefs = UserPreferencesRepository(application)
    val trackingManager = TrackingManager.getInstance(application)

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    val unitSystem = prefs.unitSystemFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        UnitSystem.METRIC
    )

    val dailyGoalKm = prefs.dailyGoalKmFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        5.0
    )

    val dailyStepsGoal = prefs.dailyStepsGoalFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        7500
    )

    val userWeightKg = prefs.userWeightKgFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        70.0f
    )

    val gpsAccuracyThreshold = prefs.gpsAccuracyThresholdFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        35.0f
    )

    val allWalks = walkDao.getAllFlow().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val trackingStatus = trackingManager.status
    val liveMetrics = trackingManager.metrics

    // Today's total distance & steps
    val todayStats = combine(allWalks) { walksList ->
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis

        val todayWalks = walksList.first().filter { it.startTimeMillis >= startOfDay }
        val distance = todayWalks.sumOf { it.distanceMeters }
        val steps = todayWalks.sumOf { it.steps }
        val duration = todayWalks.sumOf { it.durationSeconds }
        val calories = todayWalks.sumOf { it.calories }

        Triple(distance, steps, Pair(duration, calories))
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        Triple(0.0, 0, Pair(0L, 0))
    )

    // Weekly distances for chart (Mon - Sun of current week)
    val weeklyDistances = combine(allWalks) { walksList ->
        val walks = walksList.first()
        val daysLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val result = mutableListOf<Pair<String, Double>>()
        for (i in 0 until 7) {
            val dayStart = cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val dayEnd = cal.timeInMillis

            val dayDistance = walks.filter { it.startTimeMillis in dayStart until dayEnd }
                .sumOf { it.distanceMeters } / 1000.0
            result.add(Pair(daysLabels[i], dayDistance))
        }
        result
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        listOf("Mon" to 0.0, "Tue" to 0.0, "Wed" to 0.0, "Thu" to 0.0, "Fri" to 0.0, "Sat" to 0.0, "Sun" to 0.0)
    )

    // Personal Records
    val personalRecords = combine(allWalks) { walksList ->
        val walks = walksList.first()
        if (walks.isEmpty()) {
            PersonalRecords()
        } else {
            val longestWalk = walks.maxOfOrNull { it.distanceMeters } ?: 0.0
            val fastestPace = walks.filter { it.avgPaceSecondsPerKm > 0 }
                .minOfOrNull { it.avgPaceSecondsPerKm } ?: 0.0
            val longestDur = walks.maxOfOrNull { it.durationSeconds } ?: 0L
            val totalDist = walks.sumOf { it.distanceMeters }
            val totalCal = walks.sumOf { it.calories }
            val totalStp = walks.sumOf { it.steps }

            PersonalRecords(
                longestWalkMeters = longestWalk,
                fastestPaceSecPerKm = fastestPace,
                longestDurationSeconds = longestDur,
                totalDistanceMeters = totalDist,
                totalWalksCount = walks.size,
                totalCalories = totalCal,
                totalSteps = totalStp
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PersonalRecords()
    )

    init {
        viewModelScope.launch {
            userWeightKg.collect { weight ->
                trackingManager.setWeightKg(weight)
            }
        }
        viewModelScope.launch {
            gpsAccuracyThreshold.collect { threshold ->
                trackingManager.setAccuracyThreshold(threshold)
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun startWorkout() {
        trackingManager.startTracking()
        TrackingService.startService(getApplication())
        _currentScreen.value = Screen.LiveWorkout
    }

    fun pauseWorkout() {
        trackingManager.pauseTracking()
    }

    fun resumeWorkout() {
        trackingManager.resumeTracking()
    }

    fun finishWorkout() {
        val finalMetrics = trackingManager.stopTracking()
        TrackingService.stopService(getApplication())

        val startTime = System.currentTimeMillis() - (finalMetrics.durationSeconds * 1000)
        val walk = WalkEntity(
            startTimeMillis = startTime,
            endTimeMillis = System.currentTimeMillis(),
            durationSeconds = finalMetrics.durationSeconds,
            distanceMeters = finalMetrics.distanceMeters,
            calories = finalMetrics.calories,
            steps = finalMetrics.steps,
            avgPaceSecondsPerKm = finalMetrics.avgPaceSecondsPerKm,
            maxSpeedMps = 0.0,
            polylineEncoded = WalkEntity.encodePoints(finalMetrics.routePoints),
            title = "Outdoor Walk",
            avgHeartRateBpm = finalMetrics.avgHeartRateBpm ?: 0,
            maxHeartRateBpm = finalMetrics.maxHeartRateBpm ?: 0
        )
        _currentScreen.value = Screen.WorkoutSummary(walk)
    }

    // BLE Heart Rate Monitor Integration
    val bleManager = trackingManager.bleHeartRateManager
    val bleConnectionState = bleManager.connectionState
    val connectedHrDeviceName = bleManager.connectedDeviceName
    val scannedHrDevices = bleManager.scannedDevices
    val currentHeartRate = bleManager.currentHeartRate
    val heartRateZone = bleManager.heartRateZone
    val isBleDemoMode = bleManager.isDemoMode

    fun startBleScan() = bleManager.startScan()
    fun stopBleScan() = bleManager.stopScan()
    fun connectBleDevice(address: String) = bleManager.connectToDevice(address)
    fun disconnectBleDevice() = bleManager.disconnect()
    fun startVirtualHrSensor() = bleManager.startDemoSensor()

    fun saveWorkout(walk: WalkEntity, title: String, notes: String) {
        viewModelScope.launch {
            val updated = walk.copy(
                title = if (title.isBlank()) "Outdoor Walk" else title,
                notes = notes
            )
            walkDao.insert(updated)
            trackingManager.reset()
            _currentScreen.value = Screen.Home
        }
    }

    fun discardWorkout() {
        trackingManager.reset()
        _currentScreen.value = Screen.Home
    }

    fun deleteWalk(walk: WalkEntity) {
        viewModelScope.launch {
            walkDao.delete(walk)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            walkDao.deleteAll()
        }
    }

    fun updateUnitSystem(system: UnitSystem) {
        viewModelScope.launch {
            prefs.setUnitSystem(system)
        }
    }

    fun updateDailyGoalKm(goalKm: Double) {
        viewModelScope.launch {
            prefs.setDailyGoalKm(goalKm)
        }
    }

    fun updateDailyStepsGoal(steps: Int) {
        viewModelScope.launch {
            prefs.setDailyStepsGoal(steps)
        }
    }

    fun updateUserWeight(weight: Float) {
        viewModelScope.launch {
            prefs.setUserWeightKg(weight)
        }
    }

    fun updateGpsThreshold(threshold: Float) {
        viewModelScope.launch {
            prefs.setGpsAccuracyThreshold(threshold)
        }
    }
}
