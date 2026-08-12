package Ir.co.tfs.farazaman.domain.validation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginInputValidatorTest {
    @Test
    fun validCredentials_areAccepted() {
        assertTrue(LoginInputValidator.isValid("بازرس", "رمز-معتبر"))
    }

    @Test
    fun blankUsername_isRejected() {
        assertFalse(LoginInputValidator.isValid("   ", "رمز-معتبر"))
    }

    @Test
    fun emptyPassword_isRejected() {
        assertFalse(LoginInputValidator.isValid("بازرس", ""))
    }
}
