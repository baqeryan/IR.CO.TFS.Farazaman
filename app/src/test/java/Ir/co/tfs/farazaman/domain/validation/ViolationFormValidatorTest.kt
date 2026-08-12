package Ir.co.tfs.farazaman.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ViolationFormValidatorTest {
    @Test
    fun invalidCount_isRejectedFirst() {
        assertEquals(
            ViolationFormValidator.Error.INVALID_COUNT,
            ViolationFormValidator.validateBasic(0, "خیابان آزادی", "توضیحات"),
        )
    }

    @Test
    fun blankAddress_isRejected() {
        assertEquals(
            ViolationFormValidator.Error.EMPTY_ADDRESS,
            ViolationFormValidator.validateBasic(1, " ", "توضیحات"),
        )
    }

    @Test
    fun blankDescription_isRejected() {
        assertEquals(
            ViolationFormValidator.Error.EMPTY_DESCRIPTION,
            ViolationFormValidator.validateBasic(1, "خیابان آزادی", ""),
        )
    }

    @Test
    fun validForm_hasNoBasicError() {
        assertNull(ViolationFormValidator.validateBasic(2, "خیابان آزادی", "نمونه تخلف"))
    }
}
