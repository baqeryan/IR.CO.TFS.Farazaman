package Ir.co.tfs.farazaman.util

import org.junit.Assert.assertEquals
import org.junit.Test

class RoadQueryBufferTest {

    @Test
    fun goodAccuracy_usesExpandedGoodBuffer() {
        assertEquals(20, RoadQueryBuffer.forAccuracy(10f))
    }

    @Test
    fun weakOrMissingAccuracy_usesExpandedDefaultBuffer() {
        assertEquals(50, RoadQueryBuffer.forAccuracy(30f))
        assertEquals(50, RoadQueryBuffer.forAccuracy(null))
    }
}
