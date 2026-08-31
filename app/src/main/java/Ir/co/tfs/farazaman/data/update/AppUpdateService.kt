package Ir.co.tfs.farazaman.data.update

import Ir.co.tfs.farazaman.data.model.AppUpdateInfo
import Ir.co.tfs.farazaman.di.AppUpdateClient
import Ir.co.tfs.farazaman.util.NetworkTimeouts
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface AppUpdateService {
    suspend fun fetchLatestVersion(): AppUpdateInfo

    suspend fun downloadApk(
        downloadUrl: String,
        destination: File,
        onProgress: (Int) -> Unit,
    )
}

class OkHttpAppUpdateService @Inject constructor(
    @AppUpdateClient private val client: OkHttpClient,
    private val parser: AppUpdateJsonParser,
) : AppUpdateService {

    override suspend fun fetchLatestVersion(): AppUpdateInfo = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(AppUpdateConfig.VERSION_URL)
            .get()
            .header("Accept", "application/json")
            .build()

        val call = client.newCall(request)
        call.timeout().timeout(NetworkTimeouts.UPDATE_CHECK_CALL_SECONDS, TimeUnit.SECONDS)
        call.execute().use { response ->
            check(response.isSuccessful) { "Update API returned HTTP ${response.code}" }
            val body = response.body?.string().orEmpty()
            check(body.isNotBlank()) { "Update API returned an empty body" }
            parser.parse(body)
        }
    }

    override suspend fun downloadApk(
        downloadUrl: String,
        destination: File,
        onProgress: (Int) -> Unit,
    ) = withContext(Dispatchers.IO) {
        check(AppUpdateConfig.isTrustedDownloadUrl(downloadUrl)) { "Untrusted APK URL" }
        val request = Request.Builder().url(downloadUrl).get().build()

        client.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "APK download returned HTTP ${response.code}" }
            val body = response.body ?: error("APK download returned an empty body")
            val totalBytes = body.contentLength()
            var copiedBytes = 0L
            var lastProgress = -1

            FileOutputStream(destination, false).use { output ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        output.write(buffer, 0, count)
                        copiedBytes += count
                        if (totalBytes > 0) {
                            val progress = ((copiedBytes * 100L) / totalBytes).toInt().coerceIn(0, 100)
                            if (progress != lastProgress) {
                                lastProgress = progress
                                onProgress(progress)
                            }
                        }
                    }
                    output.fd.sync()
                }
            }
            check(copiedBytes > 0) { "Downloaded APK is empty" }
            onProgress(100)
        }
    }
}
