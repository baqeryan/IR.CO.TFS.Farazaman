package Ir.co.tfs.farazaman.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TokenExpiryPolicyTest {
    @Test
    fun missingExpiry_isExpired() {
        assertTrue(TokenExpiryPolicy.isExpired(0L, 1_000L))
    }

    @Test
    fun tokenOutsideRefreshBuffer_isValid() {
        assertFalse(TokenExpiryPolicy.isExpired(expiresAtMs = 100_000L, nowMs = 60_000L))
    }

    @Test
    fun tokenAtRefreshBufferBoundary_isExpired() {
        assertTrue(TokenExpiryPolicy.isExpired(expiresAtMs = 100_000L, nowMs = 70_000L))
    }
}
