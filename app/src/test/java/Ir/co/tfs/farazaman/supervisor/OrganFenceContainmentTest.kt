package Ir.co.tfs.farazaman.supervisor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OrganFenceContainmentTest {

    private val square = "POLYGON ((50 35, 51 35, 51 36, 50 36, 50 35))"

    @Test
    fun detectsInsideOutsideAndBoundary() {
        assertEquals(true, OrganFenceContainment.contains(square, 35.5, 50.5))
        assertEquals(false, OrganFenceContainment.contains(square, 37.0, 50.5))
        assertEquals(true, OrganFenceContainment.contains(square, 35.0, 50.5))
    }

    @Test
    fun excludesPolygonHoles() {
        val withHole = "POLYGON ((50 35, 52 35, 52 37, 50 37, 50 35), " +
            "(50.5 35.5, 51.5 35.5, 51.5 36.5, 50.5 36.5, 50.5 35.5))"

        assertEquals(false, OrganFenceContainment.contains(withHole, 36.0, 51.0))
        assertEquals(true, OrganFenceContainment.contains(withHole, 35.25, 50.25))
    }

    @Test
    fun rejectsMissingOrUnsupportedFence() {
        assertNull(OrganFenceContainment.contains("", 35.0, 50.0))
        assertNull(OrganFenceContainment.contains("LINESTRING (50 35, 51 36)", 35.0, 50.0))
    }
}
