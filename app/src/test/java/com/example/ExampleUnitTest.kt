package com.example

import com.example.data.db.WalkEntity
import com.example.data.model.GeoPoint
import com.example.data.model.UnitSystem
import com.example.util.GpsMetricsCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testHaversineDistance() {
        // Distance between two known coordinates (approx 111.19 km for 1 deg latitude)
        val lat1 = 37.7749
        val lon1 = -122.4194
        val lat2 = 37.7750
        val lon2 = -122.4194
        val distance = GpsMetricsCalculator.calculateHaversineDistance(lat1, lon1, lat2, lon2)
        // 0.0001 deg latitude is approximately 11.1 meters
        assertTrue(distance > 10.0 && distance < 12.0)
    }

    @Test
    fun testOutlierRejection() {
        val p1 = GeoPoint(37.7749, -122.4194, accuracy = 5f, timestampMillis = 1000L)
        // Micro-jitter: 0.5 meter movement should be rejected while stationary
        val p2Jitter = GeoPoint(37.77490001, -122.4194, accuracy = 5f, timestampMillis = 2000L)
        assertFalse(GpsMetricsCalculator.isReliableMovement(p1, p2Jitter))

        // Inaccurate fix: 45m accuracy (above 35m threshold) should be rejected
        val p3BadAcc = GeoPoint(37.7755, -122.4194, accuracy = 50f, timestampMillis = 3000L)
        assertFalse(GpsMetricsCalculator.isReliableMovement(p1, p3BadAcc))

        // Genuine walking fix: 20 meters over 15 seconds with good accuracy
        val p4Genuine = GeoPoint(37.77508, -122.4194, accuracy = 8f, timestampMillis = 16000L)
        assertTrue(GpsMetricsCalculator.isReliableMovement(p1, p4Genuine))
    }

    @Test
    fun testCalorieCalculation() {
        // 30 min (1800s) brisk walk of 2.5 km for 70kg person should burn roughly 120-160 kcal
        val cal = GpsMetricsCalculator.calculateCalories(1800L, 2500.0, 70.0f)
        assertTrue("Calories burned should be reasonable ($cal)", cal in 100..200)
    }

    @Test
    fun testStepCalculation() {
        // 1000 meters / ~0.78m average stride = ~1282 steps
        val steps = GpsMetricsCalculator.calculateSteps(1000.0)
        assertEquals(1282, steps)
    }

    @Test
    fun testDurationFormatting() {
        assertEquals("00:45", GpsMetricsCalculator.formatDuration(45L))
        assertEquals("05:30", GpsMetricsCalculator.formatDuration(330L))
        assertEquals("01:15:20", GpsMetricsCalculator.formatDuration(4520L))
    }

    @Test
    fun testPaceFormatting() {
        // 300 seconds per km = 5'00" /km
        assertEquals("5'00\" /km", GpsMetricsCalculator.formatPace(300.0, UnitSystem.METRIC))
    }

    @Test
    fun testWalkEntityPointSerialization() {
        val points = listOf(
            GeoPoint(37.7749, -122.4194, 15.0, 4.0f, 1.4f, 10000L),
            GeoPoint(37.7755, -122.4185, 16.0, 5.0f, 1.3f, 20000L)
        )
        val encoded = WalkEntity.encodePoints(points)
        val entity = WalkEntity(
            startTimeMillis = 10000L,
            endTimeMillis = 20000L,
            durationSeconds = 10L,
            distanceMeters = 100.0,
            calories = 10,
            steps = 120,
            avgPaceSecondsPerKm = 300.0,
            maxSpeedMps = 1.4,
            polylineEncoded = encoded
        )
        val decoded = entity.parsePoints()
        assertEquals(2, decoded.size)
        assertEquals(37.7749, decoded[0].latitude, 0.00001)
        assertEquals(-122.4185, decoded[1].longitude, 0.00001)
    }

    @Test
    fun testHeartRateZones() {
        // Max HR 185
        // 90 bpm -> < 60% -> REST / Warm Up
        assertEquals(com.example.data.model.HeartRateZone.REST, com.example.data.model.HeartRateZone.fromBpm(90, 185))
        // 120 bpm -> 64.8% -> FAT_BURN
        assertEquals(com.example.data.model.HeartRateZone.FAT_BURN, com.example.data.model.HeartRateZone.fromBpm(120, 185))
        // 140 bpm -> 75.6% -> AEROBIC
        assertEquals(com.example.data.model.HeartRateZone.AEROBIC, com.example.data.model.HeartRateZone.fromBpm(140, 185))
        // 160 bpm -> 86.4% -> THRESHOLD
        assertEquals(com.example.data.model.HeartRateZone.THRESHOLD, com.example.data.model.HeartRateZone.fromBpm(160, 185))
        // 175 bpm -> 94.5% -> PEAK
        assertEquals(com.example.data.model.HeartRateZone.PEAK, com.example.data.model.HeartRateZone.fromBpm(175, 185))
    }

    @Test
    fun testBleGattPacketParsing() {
        // Bluetooth SIG HR Measurement:
        // Format 1: 8-bit (Flags byte = 0x00, Byte 1 = BPM)
        val packet8Bit = byteArrayOf(0x00, 125.toByte())
        val flags8 = packet8Bit[0].toInt()
        val is16Bit8 = (flags8 and 0x01) != 0
        assertFalse(is16Bit8)
        val bpm8 = packet8Bit[1].toInt() and 0xFF
        assertEquals(125, bpm8)

        // Format 2: 16-bit (Flags byte = 0x01, Byte 1 = LSB, Byte 2 = MSB)
        // 150 BPM in 16-bit = 0x0096 -> LSB 0x96, MSB 0x00
        val packet16Bit = byteArrayOf(0x01, 0x96.toByte(), 0x00)
        val flags16 = packet16Bit[0].toInt()
        val is16Bit16 = (flags16 and 0x01) != 0
        assertTrue(is16Bit16)
        val bpm16 = ((packet16Bit[2].toInt() and 0xFF) shl 8) or (packet16Bit[1].toInt() and 0xFF)
        assertEquals(150, bpm16)
    }
}
