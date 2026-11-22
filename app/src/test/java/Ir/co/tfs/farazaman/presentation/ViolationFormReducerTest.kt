package Ir.co.tfs.farazaman.presentation

import Ir.co.tfs.farazaman.presentation.compose.ViolationFormMutation
import Ir.co.tfs.farazaman.presentation.compose.ViolationFormReducer
import Ir.co.tfs.farazaman.presentation.compose.ViolationFormUiState
import Ir.co.tfs.farazaman.presentation.compose.normalizeViolationNumericInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class ViolationFormReducerTest {
    @Test
    fun `count accepts only digits and normalizes localized numbers`() {
        assertEquals("123456", normalizeViolationNumericInput("۱٢3 مقدار ۴-5۶"))
    }

    @Test
    fun `editing a field clears stale validation error`() {
        val initial = ViolationFormUiState(description = "", fieldError = "توضیحات الزامی است")
        val result = ViolationFormReducer.reduce(
            initial,
            ViolationFormMutation.DescriptionChanged("شرح نقص"),
        )
        assertEquals("شرح نقص", result.description)
        assertNull(result.fieldError)
    }

    @Test
    fun `validation failure stops submitting`() {
        val initial = ViolationFormUiState(submitting = true)
        val result = ViolationFormReducer.reduce(
            initial,
            ViolationFormMutation.ValidationFailed("مقدار معتبر نیست"),
        )
        assertFalse(result.submitting)
        assertEquals("مقدار معتبر نیست", result.fieldError)
    }

    @Test
    fun `image count cannot become negative`() {
        val result = ViolationFormReducer.reduce(
            ViolationFormUiState(),
            ViolationFormMutation.ImageCountChanged(-2),
        )
        assertEquals(0, result.imageCount)
    }
}
