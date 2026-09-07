package Ir.co.tfs.farazaman.supervisor

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SupervisorMissionDayPolicyTest {

    @Test
    fun `same mission day does not require registration`() {
        assertFalse(SupervisorMissionDayPolicy.requiresRegistration("2026-09-05", "2026-09-05"))
    }

    @Test
    fun `previous mission day requires registration`() {
        assertTrue(SupervisorMissionDayPolicy.requiresRegistration("2026-09-04", "2026-09-05"))
    }

    @Test
    fun `same ISO date with time does not require registration`() {
        assertFalse(
            SupervisorMissionDayPolicy.requiresRegistration(
                "2026-09-05T00:00:00",
                "2026-09-05",
            ),
        )
    }

    @Test
    fun `missing mission date requires registration`() {
        assertTrue(SupervisorMissionDayPolicy.requiresRegistration(null, "2026-09-05"))
    }
}
