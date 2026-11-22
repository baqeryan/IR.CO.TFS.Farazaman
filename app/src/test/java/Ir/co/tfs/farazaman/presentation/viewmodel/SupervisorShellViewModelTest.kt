package Ir.co.tfs.farazaman.presentation.viewmodel

import Ir.co.tfs.farazaman.presentation.compose.SupervisorShellEvent
import Ir.co.tfs.farazaman.presentation.compose.SupervisorShellReducer
import Ir.co.tfs.farazaman.presentation.compose.SupervisorShellTab
import Ir.co.tfs.farazaman.presentation.compose.SupervisorShellUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class SupervisorShellViewModelTest {

    @Test
    fun `select tab updates only selected tab`() {
        val initial = SupervisorShellUiState(
            selectedTab = SupervisorShellTab.Home,
            missionTitle = "ماموریت فعال",
            isTracking = true,
        )

        val result = SupervisorShellReducer.reduce(
            initial,
            SupervisorShellEvent.SelectTab(SupervisorShellTab.History),
        )

        assertEquals(SupervisorShellTab.History, result.selectedTab)
        assertEquals(initial.missionTitle, result.missionTitle)
        assertEquals(initial.isTracking, result.isTracking)
    }

    @Test
    fun `side effect events do not optimistically mutate state`() {
        val initial = SupervisorShellUiState(isTracking = false)

        assertSame(
            initial,
            SupervisorShellReducer.reduce(initial, SupervisorShellEvent.StartVisit),
        )
        assertSame(
            initial,
            SupervisorShellReducer.reduce(initial, SupervisorShellEvent.OpenNotifications),
        )
    }
}
