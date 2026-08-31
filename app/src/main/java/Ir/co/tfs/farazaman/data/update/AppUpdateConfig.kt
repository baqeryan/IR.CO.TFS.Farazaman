package Ir.co.tfs.farazaman.data.update

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object AppUpdateConfig {
    // Update metadata controls executable delivery and therefore must be served over TLS.
    const val VERSION_URL = "https://farazaman.tfs.co.ir/api/app/version"
    private const val TRUSTED_DOWNLOAD_HOST = "ftp.tfs.co.ir"

    fun isTrustedDownloadUrl(value: String): Boolean {
        val url = value.toHttpUrlOrNull() ?: return false
        return url.isHttps && url.host.equals(TRUSTED_DOWNLOAD_HOST, ignoreCase = true)
    }
}
