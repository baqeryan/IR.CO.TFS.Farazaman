package Ir.co.tfs.farazaman.domain.tracking

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackingPointPolicyTest {
    @Test
    fun firstAccuratePoint_isLogged() {
        assertTrue(TrackingPointPolicy.shouldLogMovement(false, 0f, 5f, 10f, 1f))
    }

    @Test
    fun inaccurateWalkingPoint_isRejected() {
        assertFalse(TrackingPointPolicy.shouldLogMovement(true, 10f, 5f, 31f, 1f))
    }

    @Test
    fun movementBelowMinimum_isRejected() {
        assertFalse(TrackingPointPolicy.shouldLogMovement(true, 4.99f, 5f, 10f, 1f))
    }

    @Test
    fun accurateMovementAtMinimum_isLogged() {
        assertTrue(TrackingPointPolicy.shouldLogMovement(true, 5f, 5f, 10f, 1f))
    }
}
