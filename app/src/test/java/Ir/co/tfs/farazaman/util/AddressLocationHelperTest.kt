package Ir.co.tfs.farazaman.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddressLocationHelperTest {
    @Test
    fun firstPoint_requestsAddress() {
        assertTrue(AddressLocationHelper.shouldUpdateAddress(null, 35.7 to 51.4))
    }

    @Test
    fun movementBelowFiveMeters_doesNotRequestAddress() {
        val start = 35.7 to 51.4
        val aboutFourMetersNorth = 35.700036 to 51.4
        assertFalse(AddressLocationHelper.shouldUpdateAddress(start, aboutFourMetersNorth))
    }

    @Test
    fun movementBeyondFiveMeters_requestsAddress() {
        val start = 35.7 to 51.4
        val aboutSixMetersNorth = 35.700054 to 51.4
        assertTrue(AddressLocationHelper.shouldUpdateAddress(start, aboutSixMetersNorth))
    }
}
