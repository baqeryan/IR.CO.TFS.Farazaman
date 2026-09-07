package Ir.co.tfs.farazaman.data.model

import Ir.co.tfs.farazaman.data.db.model.TrackPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class ViolationGeoLocationMapperTest {

    @Test
    fun create_usesRecordedTrackPointValues() {
        val point = TrackPoint().apply {
            latitude = 36.5659
            longitude = 53.0586
            elevation = 42.5
            accuracy = 7.25
            speed = 3.5
            compassHeading = 125.0
            atmosphericPressure = 1008.4
            pointTimestamp = 1_700_000_000_000L
        }

        val result = ViolationGeoLocationMapper.create("fallback", point)
        val track = result.gisGeolocationTrack

        assertEquals("{\"type\":\"Point\",\"coordinates\":[53.0586,36.5659]}", result.geoJson)
        assertEquals("POINT(53.0586 36.5659)", result.wellKnownText)
        assertEquals(36.5659, track.latitude, 0.0)
        assertEquals(53.0586, track.longitude, 0.0)
        assertEquals(42.5, track.elevation!!, 0.0)
        assertEquals(7.25, track.accuracy!!, 0.0)
        assertEquals(3.5, track.speed!!, 0.0)
        assertEquals(125.0, track.heading!!, 0.0)
        assertEquals(1008.4, track.cBaro!!, 0.0)
        assertEquals("2023-11-14T22:13:20Z", track.time)
        assertNull(track.altitudeAccuracy)
        assertNull(track.satelliteNumber)
    }

    @Test
    fun create_doesNotInventMissingSensorValues() {
        val point = TrackPoint().apply {
            latitude = 35.7
            longitude = 51.4
        }

        val track = ViolationGeoLocationMapper.create("2026-09-05T10:30:00", point)
            .gisGeolocationTrack

        assertEquals("2026-09-05T10:30:00", track.time)
        assertNull(track.elevation)
        assertNull(track.accuracy)
        assertNull(track.speed)
        assertNull(track.heading)
        assertNull(track.cBaro)
    }

    @Test
    fun zeroCoordinates_areInvalidInsteadOfUsingAFixedFallback() {
        assertFalse(ViolationGeoLocationMapper.hasValidCoordinates(TrackPoint()))
    }
}
