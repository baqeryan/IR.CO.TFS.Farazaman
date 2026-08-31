package Ir.co.tfs.farazaman.data.repository

import Ir.co.tfs.farazaman.data.model.AppUpdateInfo
import Ir.co.tfs.farazaman.data.update.ApkFileValidator
import Ir.co.tfs.farazaman.data.update.AppUpdateService
import android.content.Context
import java.io.File
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class AppUpdateRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `API failure is returned without blocking startup`() = runBlocking {
        val repository = repository(FakeService(fetchFailure = IOException("offline")))

        assertTrue(repository.checkForUpdate(65) is UpdateCheckResult.Failed)
    }

    @Test
    fun `download failure removes partial file and retry can succeed`() = runBlocking {
        val service = FakeService(failFirstDownload = true)
        val repository = repository(service)

        val first = runCatching { repository.download(update(), {}) }
        assertTrue(first.isFailure)
        assertFalse(File(temporaryFolder.root, "app_updates/farazaman-66.apk.part").exists())

        val second = repository.download(update(), {})
        assertTrue(second.isFile)
        assertTrue(second.name.endsWith(".apk"))
    }

    private fun repository(service: AppUpdateService): AppUpdateRepository {
        val context = mock(Context::class.java)
        `when`(context.cacheDir).thenReturn(temporaryFolder.root)
        val validator = object : ApkFileValidator {
            override fun validate(file: File, expected: AppUpdateInfo) {
                require(file.length() > 0)
            }
        }
        return AppUpdateRepository(service, validator, context)
    }

    private fun update() = AppUpdateInfo(
        versionCode = 66,
        versionName = "1.2.3",
        downloadUrl = "https://ftp.tfs.co.ir/farazaman.apk",
        mandatory = false,
        releaseNotes = null,
        publishedAt = null,
    )

    private class FakeService(
        private val fetchFailure: Exception? = null,
        private val failFirstDownload: Boolean = false,
    ) : AppUpdateService {
        private var downloadCalls = 0

        override suspend fun fetchLatestVersion(): AppUpdateInfo {
            fetchFailure?.let { throw it }
            return AppUpdateInfo(
                versionCode = 66,
                versionName = "1.2.3",
                downloadUrl = "https://ftp.tfs.co.ir/farazaman.apk",
                mandatory = false,
                releaseNotes = null,
                publishedAt = null,
            )
        }

        override suspend fun downloadApk(
            downloadUrl: String,
            destination: File,
            onProgress: (Int) -> Unit,
        ) {
            downloadCalls++
            destination.writeText("partial")
            if (failFirstDownload && downloadCalls == 1) throw IOException("connection lost")
            onProgress(100)
        }
    }
}
