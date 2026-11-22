package Ir.co.tfs.farazaman.presentation.design.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FzFormContractsTest {
    @Test fun numericMasksRejectInvalidCharacters() {
        assertEquals("۱23", FzInputMasks.Digits.filter("۱a2-3"))
        assertEquals("12.34", FzInputMasks.Decimal.filter("12.3.4"))
    }

    @Test fun persianDatesHaveStableOrderingAndFormatting() {
        val start = FzPersianDate(1405, 1, 2)
        val end = FzPersianDate(1405, 2, 1)
        assertTrue(start < end)
        assertEquals("1405/01/02", start.toString())
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidPersianDateIsRejected() {
        FzPersianDate(1405, 13, 1)
    }
}
