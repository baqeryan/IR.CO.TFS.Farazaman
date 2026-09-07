package Ir.co.tfs.farazaman.activity

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts

/** Camera-only image capture shared by violation forms. */
class ViolationImageCaptureCoordinator(
    caller: ActivityResultCaller,
    private val contentResolver: () -> ContentResolver,
    private val onCaptured: (uri: Uri, replaceIndex: Int?) -> Unit,
    private val onCaptureFailed: () -> Unit,
) {
    private var pendingUri: Uri? = null
    private var pendingReplaceIndex: Int? = null

    private val launcher = caller.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = pendingUri
        val replaceIndex = pendingReplaceIndex
        pendingUri = null
        pendingReplaceIndex = null

        if (success && uri != null && isReadable(uri)) {
            publish(uri)
            onCaptured(uri, replaceIndex)
        } else {
            uri?.let(::deleteQuietly)
            onCaptureFailed()
        }
    }

    fun captureNew(): Boolean = launchCapture(replaceIndex = null)

    fun captureReplacement(replaceIndex: Int): Boolean = launchCapture(replaceIndex)

    private fun launchCapture(replaceIndex: Int?): Boolean {
        if (pendingUri != null) return false
        val uri = runCatching {
            contentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "farazaman_${System.currentTimeMillis()}.jpg")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.TITLE, "FaraZaman violation evidence")
                    put(MediaStore.Images.Media.DESCRIPTION, "Captured with FaraZaman camera")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                },
            )
        }.getOrNull()

        if (uri == null) {
            onCaptureFailed()
            return false
        }

        pendingUri = uri
        pendingReplaceIndex = replaceIndex
        launcher.launch(uri)
        return true
    }

    private fun isReadable(uri: Uri): Boolean = runCatching {
        contentResolver().openFileDescriptor(uri, "r")?.use { descriptor ->
            descriptor.fileDescriptor.valid()
        } == true
    }.getOrDefault(false)

    private fun publish(uri: Uri) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        runCatching {
            contentResolver().update(
                uri,
                ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) },
                null,
                null,
            )
        }
    }

    private fun deleteQuietly(uri: Uri) {
        runCatching { contentResolver().delete(uri, null, null) }
    }
}
