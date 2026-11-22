package Ir.co.tfs.farazaman.activity

import android.content.ContentUris
import android.content.ContentValues
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import Ir.co.tfs.farazaman.data.db.DataHelper
import Ir.co.tfs.farazaman.data.db.TrackContentProvider
import Ir.co.tfs.farazaman.gpx.ExportToTempFileTask
import Ir.co.tfs.farazaman.gpx.ZipHelper
import Ir.co.tfs.farazaman.presentation.compose.track.TrackUploadEvent
import Ir.co.tfs.farazaman.presentation.compose.track.TrackUploadScreen
import Ir.co.tfs.farazaman.presentation.compose.track.TrackUploadUiState
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import Ir.co.tfs.farazaman.util.SelectedServerUrl
import Ir.co.tfs.farazaman.util.TokenManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class OpenStreetMapUpload : ComponentActivity() {
    @Inject lateinit var okHttpClient: OkHttpClient
    @Inject lateinit var tokenManager: TokenManager

    private val state = mutableStateOf(TrackUploadUiState(trackId = -1L))
    private var startDate = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val trackId = intent.getLongExtra(TrackContentProvider.Schema.COL_TRACK_ID, -1L)
        require(trackId >= 0L) { "Missing Track ID" }
        state.value = state.value.copy(trackId = trackId)
        setContent { FaraZamanTheme { TrackUploadScreen(state.value, ::onEvent) } }
        lifecycleScope.launch { loadTrack() }
    }

    private fun onEvent(event: TrackUploadEvent) {
        state.value = when (event) {
            TrackUploadEvent.Back -> { finish(); state.value }
            is TrackUploadEvent.NameChanged -> state.value.copy(name = event.value, error = null)
            is TrackUploadEvent.DescriptionChanged -> state.value.copy(description = event.value, error = null)
            is TrackUploadEvent.TagsChanged -> state.value.copy(tags = event.value)
            is TrackUploadEvent.VisibilityChanged -> state.value.copy(visibility = event.value)
            TrackUploadEvent.Submit -> {
                if (state.value.description.isBlank()) {
                    state.value.copy(error = "توضیحات مسیر الزامی است")
                } else {
                    saveAndUpload()
                    state.value.copy(uploading = true, error = null)
                }
            }
        }
    }

    private suspend fun loadTrack() {
        val loaded = withContext(Dispatchers.IO) {
            val uri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, state.value.trackId)
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (!cursor.moveToFirst()) return@withContext null
                startDate = cursor.getLong(cursor.getColumnIndexOrThrow(TrackContentProvider.Schema.COL_START_DATE))
                TrackUploadUiState(
                    trackId = state.value.trackId,
                    name = cursor.getString(cursor.getColumnIndexOrThrow(TrackContentProvider.Schema.COL_NAME)).orEmpty(),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(TrackContentProvider.Schema.COL_DESCRIPTION)).orEmpty(),
                    tags = cursor.getString(cursor.getColumnIndexOrThrow(TrackContentProvider.Schema.COL_TAGS)).orEmpty(),
                    visibility = cursor.getString(cursor.getColumnIndexOrThrow(TrackContentProvider.Schema.COL_OSM_VISIBILITY)) ?: "Private",
                    visibilityOptions = listOf("Private", "Public", "Trackable", "Identifiable"),
                )
            }
        }
        if (loaded == null) {
            Toast.makeText(this, "مسیر پیدا نشد", Toast.LENGTH_SHORT).show()
            finish()
        } else state.value = loaded
    }

    private fun saveAndUpload() {
        val snapshot = state.value
        lifecycleScope.launch(Dispatchers.IO) {
            val name = snapshot.name.trim().ifEmpty { DataHelper.FILENAME_FORMATTER.format(Date(startDate)) }
            val values = ContentValues().apply {
                put(TrackContentProvider.Schema.COL_NAME, name)
                put(TrackContentProvider.Schema.COL_DESCRIPTION, snapshot.description.trim())
                put(TrackContentProvider.Schema.COL_TAGS, snapshot.tags.trim())
                put(TrackContentProvider.Schema.COL_OSM_VISIBILITY, snapshot.visibility)
            }
            val uri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, snapshot.trackId)
            contentResolver.update(uri, values, null, null)
            withContext(Dispatchers.Main) { export(snapshot.trackId) }
        }
    }

    @Suppress("DEPRECATION")
    private fun export(trackId: Long) {
        object : ExportToTempFileTask(this, trackId) {
            override fun executionCompleted(success: Boolean) {
                val gpx = tmpFile
                if (!success || gpx == null || !gpx.canRead()) {
                    uploadFailed("ساخت فایل GPX انجام نشد")
                    return
                }
                val zip = ZipHelper.zipCacheFiles(this@OpenStreetMapUpload, trackId, gpx)
                if (zip == null || !zip.canRead()) {
                    uploadFailed("ساخت فایل ZIP انجام نشد")
                    return
                }
                lifecycleScope.launch { upload(zip) }
            }
        }.execute()
    }

    private suspend fun upload(file: File) {
        val result = withContext(Dispatchers.IO) {
            runCatching {
                val token = tokenManager.getAccessToken().orEmpty()
                check(token.isNotBlank()) { "نشست کاربر معتبر نیست" }
                val prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(this@OpenStreetMapUpload)
                val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.name, file.asRequestBody("application/zip".toMediaType()))
                    .build()
                val request = Request.Builder()
                    .url(SelectedServerUrl.apiUrl(prefs, "api/GisGeolocation/upload"))
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer $token")
                    .post(body)
                    .build()
                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) error("خطای سرور: ${response.code}")
                }
            }
        }
        result.fold(
            onSuccess = {
                Toast.makeText(this, "مسیر با موفقیت بارگذاری شد", Toast.LENGTH_SHORT).show()
                finish()
            },
            onFailure = { uploadFailed(it.message ?: "بارگذاری مسیر ناموفق بود") },
        )
    }

    private fun uploadFailed(message: String) {
        state.value = state.value.copy(uploading = false, error = message)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
