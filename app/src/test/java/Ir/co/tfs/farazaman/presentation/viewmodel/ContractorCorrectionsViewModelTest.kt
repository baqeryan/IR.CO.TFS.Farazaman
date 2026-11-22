package Ir.co.tfs.farazaman.presentation.viewmodel

import Ir.co.tfs.farazaman.presentation.compose.ContractorCorrectionsReducer
import Ir.co.tfs.farazaman.presentation.compose.ContractorCorrectionsUiState
import Ir.co.tfs.farazaman.presentation.compose.ContractorViolationLocationMutation
import Ir.co.tfs.farazaman.presentation.compose.ContractorViolationLocationReducer
import Ir.co.tfs.farazaman.presentation.compose.ContractorViolationLocationUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ContractorCorrectionsViewModelTest {
    @Test
    fun `loading state is reduced without changing message`() {
        val state = ContractorCorrectionsUiState("در حال دریافت")
        val result = ContractorCorrectionsReducer.loadingChanged(state, true)
        assertTrue(result.loading)
        assertEquals(state.serviceMessage, result.serviceMessage)
    }

    @Test
    fun `map ready stores coordinates and clears error`() {
        val result = ContractorViolationLocationReducer.reduce(
            ContractorViolationLocationUiState(errorMessage = "خطا"),
            ContractorViolationLocationMutation.Ready(35.7, 51.4),
        )
        assertTrue(result.mapReady)
        assertEquals(35.7, result.latitude ?: 0.0, 0.0)
        assertEquals(51.4, result.longitude ?: 0.0, 0.0)
        assertNull(result.errorMessage)
    }

    @Test
    fun `map failure disables renderer and exposes error`() {
        val result = ContractorViolationLocationReducer.reduce(
            ContractorViolationLocationUiState(mapReady = true),
            ContractorViolationLocationMutation.Failed("نقشه در دسترس نیست"),
        )
        assertFalse(result.mapReady)
        assertEquals("نقشه در دسترس نیست", result.errorMessage)
    }
}
