package Ir.co.tfs.farazaman.domain.location

import android.location.Location
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito

class LocationFixPolicyTest {

    @Test
    fun `usable fresh fix always wins over last known fixes`() {
        val fresh = location(36.56, 52.68, 18f)
        val moreAccurateLastKnown = location(36.57, 52.69, 2f)

        val result = LocationFixPolicy.select(fresh, listOf(moreAccurateLastKnown))

        assertTrue(result is LocationFix.Fresh)
        assertSame(fresh, result?.location)
    }

    @Test
    fun `fallback chooses best positive accuracy from GPS and network`() {
        val gps = location(36.56, 52.68, 12f)
        val network = location(36.57, 52.69, 35f)

        val result = LocationFixPolicy.select(null, listOf(network, gps))

        assertTrue(result is LocationFix.LastKnown)
        assertSame(gps, result?.location)
    }

    @Test
    fun `invalid coordinates and non-positive accuracy are rejected`() {
        val invalidLatitude = location(91.0, 52.68, 5f)
        val fabricatedOrigin = location(0.0, 0.0, 5f)
        val invalidAccuracy = location(36.56, 52.68, 0f)

        assertNull(
            LocationFixPolicy.select(
                fresh = invalidLatitude,
                lastKnown = listOf(fabricatedOrigin, invalidAccuracy),
            ),
        )
    }

    @Test
    fun `unavailable inputs do not fabricate a default location`() {
        assertNull(LocationFixPolicy.select(fresh = null, lastKnown = listOf(null)))
    }

    private fun location(latitude: Double, longitude: Double, accuracy: Float): Location {
        val location = Mockito.mock(Location::class.java)
        Mockito.`when`(location.latitude).thenReturn(latitude)
        Mockito.`when`(location.longitude).thenReturn(longitude)
        Mockito.`when`(location.hasAccuracy()).thenReturn(true)
        Mockito.`when`(location.accuracy).thenReturn(accuracy)
        return location
    }
}
