package Ir.co.tfs.farazaman.presentation.viewmodel

import Ir.co.tfs.farazaman.presentation.compose.ContractorWorkAreaMutation
import Ir.co.tfs.farazaman.presentation.compose.ContractorWorkAreaReducer
import Ir.co.tfs.farazaman.presentation.compose.ContractorWorkAreaUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContractorWorkAreaViewModelTest {
    @Test
    fun `context availability enables continuation without losing message`() {
        val initial = ContractorWorkAreaUiState("در حال دریافت محدوده")
        val result = ContractorWorkAreaReducer.reduce(initial, ContractorWorkAreaMutation.ContextAvailabilityChanged(true))
        assertTrue(result.hasContext)
        assertEquals(initial.serviceMessage, result.serviceMessage)
    }

    @Test
    fun `service failure updates message and keeps selection state`() {
        val initial = ContractorWorkAreaUiState("", hasContext = false)
        val result = ContractorWorkAreaReducer.reduce(initial, ContractorWorkAreaMutation.ServiceMessageChanged("سرویس در دسترس نیست"))
        assertFalse(result.hasContext)
        assertEquals("سرویس در دسترس نیست", result.serviceMessage)
    }
}
