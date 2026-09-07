package Ir.co.tfs.farazaman.domain.validation

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.Mockito

class ContractorCorrectionValidatorTest {

    @Test
    fun `blank description is rejected before image validation`() {
        val result = ContractorCorrectionValidator.validate("   ", listOf(Mockito.mock(Uri::class.java)))

        assertEquals(ContractorCorrectionValidator.Error.EMPTY_DESCRIPTION, result)
    }

    @Test
    fun `at least one camera image is required`() {
        val result = ContractorCorrectionValidator.validate("اصلاح انجام شد", emptyList())

        assertEquals(ContractorCorrectionValidator.Error.IMAGE_REQUIRED, result)
    }

    @Test
    fun `description and captured image form a valid API-independent draft`() {
        val result = ContractorCorrectionValidator.validate(
            "اصلاح انجام شد",
            listOf(Mockito.mock(Uri::class.java)),
        )

        assertNull(result)
    }
}
