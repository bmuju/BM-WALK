package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.UnitSystem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bm_walk_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val UNIT_SYSTEM = stringPreferencesKey("unit_system")
        val DAILY_GOAL_KM = doublePreferencesKey("daily_goal_km")
        val DAILY_STEPS_GOAL = intPreferencesKey("daily_steps_goal")
        val USER_WEIGHT_KG = floatPreferencesKey("user_weight_kg")
        val GPS_ACCURACY_THRESHOLD = floatPreferencesKey("gps_accuracy_threshold")
    }

    val unitSystemFlow: Flow<UnitSystem> = context.dataStore.data.map { prefs ->
        val saved = prefs[Keys.UNIT_SYSTEM] ?: UnitSystem.METRIC.name
        try {
            UnitSystem.valueOf(saved)
        } catch (_: Exception) {
            UnitSystem.METRIC
        }
    }

    val dailyGoalKmFlow: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[Keys.DAILY_GOAL_KM] ?: 5.0
    }

    val dailyStepsGoalFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.DAILY_STEPS_GOAL] ?: 7500
    }

    val userWeightKgFlow: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[Keys.USER_WEIGHT_KG] ?: 70.0f
    }

    val gpsAccuracyThresholdFlow: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[Keys.GPS_ACCURACY_THRESHOLD] ?: 35.0f
    }

    suspend fun setUnitSystem(unitSystem: UnitSystem) {
        context.dataStore.edit { prefs ->
            prefs[Keys.UNIT_SYSTEM] = unitSystem.name
        }
    }

    suspend fun setDailyGoalKm(goalKm: Double) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DAILY_GOAL_KM] = goalKm
        }
    }

    suspend fun setDailyStepsGoal(steps: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DAILY_STEPS_GOAL] = steps
        }
    }

    suspend fun setUserWeightKg(weight: Float) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_WEIGHT_KG] = weight
        }
    }

    suspend fun setGpsAccuracyThreshold(threshold: Float) {
        context.dataStore.edit { prefs ->
            prefs[Keys.GPS_ACCURACY_THRESHOLD] = threshold
        }
    }
}
