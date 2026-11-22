package Ir.co.tfs.farazaman.activity

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.Executors

/** Camera-only image capture shared by violation forms. */
class ViolationImageCaptureCoordinator(
    caller: ActivityResultCaller,
    private val contentResolver: () -> ContentResolver,
    private val onCaptured: (uri: Uri, replaceIndex: Int?) -> Unit,
    private val onCaptureFailed: () -> Unit,
) {
    private val context = caller as? Context
        ?: error("ViolationImageCaptureCoordinator requires a Context ActivityResultCaller")
    private var pendingUri: Uri? = null
    private var pendingFile: File? = null
    private var pendingReplaceIndex: Int? = null
    private val compressionExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    private val launcher = caller.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = pendingUri
        val file = pendingFile
        val replaceIndex = pendingReplaceIndex
        pendingUri = null
        pendingFile = null
        pendingReplaceIndex = null

        // Some OEM camera apps (and a few emulator camera versions) persist the photo but
        // incorrectly return RESULT_CANCELED. Treat the actual output as the source of truth.
        if (!success) Log.w(TAG, "Camera returned a cancelled result; checking captured output")
        if (uri != null) {
            compressAndDeliver(uri, file, replaceIndex)
        } else {
            Log.e(TAG, "Camera did not produce a readable image; success=$success, uri=$uri")
            onCaptureFailed()
        }
    }

    private fun compressAndDeliver(uri: Uri, file: File?, replaceIndex: Int?) {
        compressionExecutor.execute {
            val compressed = runCatching { compressToTarget(uri, file) }
                .onFailure { error -> Log.e(TAG, "Failed to compress captured image", error) }
                .getOrDefault(false)
            mainHandler.post {
                if (compressed && isReadable(uri)) {
                    onCaptured(uri, replaceIndex)
                } else {
                    deleteQuietly(uri, file)
                    Log.e(TAG, "Captured image compression produced no readable output")
                    onCaptureFailed()
                }
            }
        }
    }

    private fun compressToTarget(uri: Uri, file: File?): Boolean {
        val resolver = contentResolver()
        val orientation = if (file != null) readOrientation(file) else readOrientation(resolver, uri)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        if (file != null) {
            BitmapFactory.decodeFile(file.absolutePath, bounds)
        } else {
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                ?: return false
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return false

        var sampleSize = 1
        while (bounds.outWidth / sampleSize > MAX_INITIAL_DIMENSION ||
            bounds.outHeight / sampleSize > MAX_INITIAL_DIMENSION
        ) {
            sampleSize *= 2
        }
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = if (file != null) {
            BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
        } else {
            resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOptions)
            }
        } ?: return false
        var bitmap = rotateIfNeeded(decoded, orientation)
        if (bitmap !== decoded) decoded.recycle()

        try {
            repeat(MAX_RESIZE_ATTEMPTS) {
                val encoded = encodeWithinTarget(bitmap)
                if (encoded != null) {
                    if (file != null) {
                        file.outputStream().use { it.write(encoded) }
                    } else {
                        resolver.openOutputStream(uri, "wt")?.use { it.write(encoded) } ?: return false
                    }
                    // AssetFileDescriptor.length is frequently UNKNOWN_LENGTH (-1) for MediaStore
                    // items even after a successful write. The encoded buffer is the authoritative
                    // size here; verify that the provider can actually read the persisted content.
                    val persisted = file?.length()?.let { it in 1..MAX_IMAGE_BYTES }
                        ?: isReadable(uri)
                    return encoded.isNotEmpty() && encoded.size <= MAX_IMAGE_BYTES && persisted
                }
                val nextWidth = (bitmap.width * RESIZE_FACTOR).toInt().coerceAtLeast(MIN_DIMENSION)
                val nextHeight = (bitmap.height * RESIZE_FACTOR).toInt().coerceAtLeast(MIN_DIMENSION)
                if (nextWidth == bitmap.width && nextHeight == bitmap.height) return false
                val resized = Bitmap.createScaledBitmap(bitmap, nextWidth, nextHeight, true)
                if (resized !== bitmap) bitmap.recycle()
                bitmap = resized
            }
            return false
        } finally {
            bitmap.recycle()
        }
    }

    private fun encodeWithinTarget(bitmap: Bitmap): ByteArray? {
        var low = MIN_JPEG_QUALITY
        var high = MAX_JPEG_QUALITY
        var best: ByteArray? = null
        while (low <= high) {
            val quality = (low + high) / 2
            val output = ByteArrayOutputStream()
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)) return null
            val bytes = output.toByteArray()
            if (bytes.size <= MAX_IMAGE_BYTES) {
                best = bytes
                low = quality + 1
            } else {
                high = quality - 1
            }
        }
        return best
    }

    private fun readOrientation(resolver: ContentResolver, uri: Uri): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return ExifInterface.ORIENTATION_NORMAL
        return runCatching {
            resolver.openFileDescriptor(uri, "r")?.use {
                ExifInterface(it.fileDescriptor).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
    }

    private fun readOrientation(file: File): Int = runCatching {
        ExifInterface(file.absolutePath).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL,
        )
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

    private fun rotateIfNeeded(bitmap: Bitmap, orientation: Int): Bitmap {
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        return if (degrees == 0f) bitmap else Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            Matrix().apply { postRotate(degrees) },
            true,
        )
    }

    fun captureNew(): Boolean = launchCapture(replaceIndex = null)

    fun captureReplacement(replaceIndex: Int): Boolean = launchCapture(replaceIndex)

    private fun launchCapture(replaceIndex: Int?): Boolean {
        if (pendingUri != null) return false
        val captureFile = runCatching {
            val directory = File(context.cacheDir, CAPTURE_DIRECTORY).apply { mkdirs() }
            File.createTempFile("farazaman_", ".jpg", directory)
        }.onFailure { error ->
            Log.e(TAG, "Unable to create camera output file", error)
        }.getOrNull()
        val uri = captureFile?.let { file ->
            runCatching {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            }.onFailure { error ->
                Log.e(TAG, "Unable to create camera output URI", error)
            }.getOrNull()
        }

        if (uri == null) {
            captureFile?.delete()
            onCaptureFailed()
            return false
        }

        pendingUri = uri
        pendingFile = captureFile
        pendingReplaceIndex = replaceIndex
        return runCatching {
            launcher.launch(uri)
            true
        }.getOrElse {
            pendingUri = null
            pendingFile = null
            pendingReplaceIndex = null
            deleteQuietly(uri, captureFile)
            onCaptureFailed()
            false
        }
    }

    private fun isReadable(uri: Uri): Boolean = runCatching {
        contentResolver().openInputStream(uri)?.use { input -> input.read() != -1 } == true
    }.onFailure { error ->
        Log.e(TAG, "Captured image is not readable: $uri", error)
    }.getOrDefault(false)

    private fun deleteQuietly(uri: Uri, file: File? = null) {
        if (file != null) runCatching { file.delete() }
        else runCatching { contentResolver().delete(uri, null, null) }
    }

    private companion object {
        const val TAG = "ViolationImageCapture"
        const val CAPTURE_DIRECTORY = "violation_images"
        const val MAX_IMAGE_BYTES = 200L * 1024L
        const val MAX_INITIAL_DIMENSION = 2048
        const val MIN_DIMENSION = 320
        const val MAX_JPEG_QUALITY = 88
        const val MIN_JPEG_QUALITY = 20
        const val MAX_RESIZE_ATTEMPTS = 10
        const val RESIZE_FACTOR = 0.82f
    }
}
