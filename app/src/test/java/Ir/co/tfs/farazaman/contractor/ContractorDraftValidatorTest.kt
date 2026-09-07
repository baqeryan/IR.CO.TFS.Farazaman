package Ir.co.tfs.farazaman.contractor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ContractorDraftValidatorTest {
    @Test
    fun `blank description is rejected before image validation`() {
        assertEquals(
            ContractorDraftValidator.Error.DESCRIPTION_REQUIRED,
            ContractorDraftValidator.validate("  ", 0),
        )
    }

    @Test
    fun `description without an image is rejected`() {
        assertEquals(
            ContractorDraftValidator.Error.IMAGE_REQUIRED,
            ContractorDraftValidator.validate("اقدام انجام شد", 0),
        )
    }

    @Test
    fun `description and image produce a valid local draft`() {
        assertNull(ContractorDraftValidator.validate("اقدام انجام شد", 1))
    }
}
