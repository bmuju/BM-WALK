package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.GeoPoint

@Entity(tableName = "walks")
data class WalkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val durationSeconds: Long,
    val distanceMeters: Double,
    val calories: Int,
    val steps: Int,
    val avgPaceSecondsPerKm: Double,
    val maxSpeedMps: Double,
    val elevationGainMeters: Double = 0.0,
    val polylineEncoded: String = "",
    val title: String = "Outdoor Walk",
    val notes: String = "",
    val avgHeartRateBpm: Int = 0,
    val maxHeartRateBpm: Int = 0
) {
    fun parsePoints(): List<GeoPoint> {
        if (polylineEncoded.isBlank()) return emptyList()
        return try {
            polylineEncoded.split(";").mapNotNull { entry ->
                val parts = entry.split(",")
                if (parts.size >= 2) {
                    val lat = parts[0].toDoubleOrNull() ?: return@mapNotNull null
                    val lng = parts[1].toDoubleOrNull() ?: return@mapNotNull null
                    val alt = if (parts.size > 2) parts[2].toDoubleOrNull() ?: 0.0 else 0.0
                    val acc = if (parts.size > 3) parts[3].toFloatOrNull() ?: 0f else 0f
                    val spd = if (parts.size > 4) parts[4].toFloatOrNull() ?: 0f else 0f
                    val time = if (parts.size > 5) parts[5].toLongOrNull() ?: 0L else 0L
                    GeoPoint(lat, lng, alt, acc, spd, time)
                } else null
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    companion object {
        fun encodePoints(points: List<GeoPoint>): String {
            return points.joinToString(";") { p ->
                "${p.latitude},${p.longitude},${p.altitude},${p.accuracy},${p.speed},${p.timestampMillis}"
            }
        }
    }
}
