package Ir.co.tfs.farazaman.presentation

import Ir.co.tfs.farazaman.presentation.compose.SubmitDedupGate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SubmitDedupGateTest {
    @Test fun `only one submission can be acquired at a time`() {
        val gate = SubmitDedupGate()
        assertTrue(gate.tryAcquire())
        assertFalse(gate.tryAcquire())
        gate.release()
        assertTrue(gate.tryAcquire())
    }
}
