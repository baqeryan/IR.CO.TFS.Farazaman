package Ir.co.tfs.farazaman.presentation.viewmodel

import Ir.co.tfs.farazaman.contractor.ContractorWorkContext
import Ir.co.tfs.farazaman.presentation.compose.ContractorActionMutation
import Ir.co.tfs.farazaman.presentation.compose.ContractorActionReducer
import Ir.co.tfs.farazaman.presentation.compose.ContractorActionUiState
import Ir.co.tfs.farazaman.presentation.compose.ContractorOperationsReducer
import Ir.co.tfs.farazaman.presentation.compose.ContractorOperationsUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ContractorOperationsViewModelTest {
    private val context = ContractorWorkContext(1, "ناحیه یک", 2, "قرارداد دو")

    @Test
    fun `operation status is server driven`() {
        val state = ContractorOperationsUiState(context, "")
        assertTrue(ContractorOperationsReducer.operationStatusChanged(state, true).operationActive)
    }

    @Test
    fun `description mutation clears previous validation error`() {
        val state = ContractorActionUiState(error = "توضیحات الزامی است", serviceMessage = "")
        val result = ContractorActionReducer.reduce(state, ContractorActionMutation.DescriptionChanged("اقدام انجام شد"))
        assertEquals("اقدام انجام شد", result.description)
        assertNull(result.error)
    }

    @Test
    fun `negative image count is normalized`() {
        val result = ContractorActionReducer.reduce(
            ContractorActionUiState(serviceMessage = ""),
            ContractorActionMutation.ImageCountChanged(-1),
        )
        assertEquals(0, result.imageCount)
    }
}
