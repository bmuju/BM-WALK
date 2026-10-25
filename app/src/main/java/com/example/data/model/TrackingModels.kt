package com.example.data.model

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val accuracy: Float = 0f,
    val speed: Float = 0f,
    val timestampMillis: Long = System.currentTimeMillis()
)

data class LiveMetrics(
    val durationSeconds: Long = 0L,
    val distanceMeters: Double = 0.0,
    val currentPaceSecondsPerKm: Double = 0.0,
    val avgPaceSecondsPerKm: Double = 0.0,
    val calories: Int = 0,
    val steps: Int = 0,
    val gpsAccuracyMeters: Float = 0f,
    val currentLocation: GeoPoint? = null,
    val routePoints: List<GeoPoint> = emptyList(),
    val currentHeartRateBpm: Int? = null,
    val avgHeartRateBpm: Int? = null,
    val maxHeartRateBpm: Int? = null,
    val hrZone: HeartRateZone? = null
)

enum class TrackingStatus {
    IDLE,
    ACQUIRING,
    TRACKING,
    PAUSED,
    STOPPED
}

enum class UnitSystem {
    METRIC,
    IMPERIAL
}

enum class BleConnectionState {
    DISCONNECTED,
    SCANNING,
    CONNECTING,
    CONNECTED
}

data class BleDeviceItem(
    val name: String,
    val address: String,
    val rssi: Int
)

enum class HeartRateZone(val label: String, val minPct: Int, val maxPct: Int) {
    REST("Warm Up", 0, 59),
    FAT_BURN("Fat Burn", 60, 69),
    AEROBIC("Aerobic", 70, 79),
    THRESHOLD("Anaerobic", 80, 89),
    PEAK("Peak", 90, 100);

    companion object {
        fun fromBpm(bpm: Int, maxHr: Int = 185): HeartRateZone {
            val pct = (bpm.toFloat() / maxHr * 100).toInt()
            return when {
                pct < 60 -> REST
                pct < 70 -> FAT_BURN
                pct < 80 -> AEROBIC
                pct < 90 -> THRESHOLD
                else -> PEAK
            }
        }
    }
}
