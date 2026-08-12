package Ir.co.tfs.farazaman.auth

object TokenExpiryPolicy {
    const val REFRESH_BUFFER_MS = 30_000L

    fun isExpired(expiresAtMs: Long, nowMs: Long): Boolean {
        return expiresAtMs <= 0L || nowMs >= expiresAtMs - REFRESH_BUFFER_MS
    }
}
