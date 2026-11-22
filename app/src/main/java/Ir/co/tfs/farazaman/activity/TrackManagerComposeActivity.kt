package Ir.co.tfs.farazaman.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import Ir.co.tfs.farazaman.data.db.TrackContentProvider
import Ir.co.tfs.farazaman.gpx.ExportToStorageTask
import Ir.co.tfs.farazaman.presentation.compose.FaraZamanTheme
import Ir.co.tfs.farazaman.presentation.compose.track.TrackListEvent
import Ir.co.tfs.farazaman.presentation.compose.track.TrackListItemUiState
import Ir.co.tfs.farazaman.presentation.compose.track.TrackListScreen
import Ir.co.tfs.farazaman.presentation.compose.track.TrackListUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date

@AndroidEntryPoint
class TrackManagerComposeActivity : ComponentActivity() {
    private val state = mutableStateOf(TrackListUiState(loading = true))
    private var pendingExportTrackId = -1L
    private val storagePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted && pendingExportTrackId >= 0L) exportTrack(pendingExportTrackId)
        pendingExportTrackId = -1L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FaraZamanTheme {
                TrackListScreen(state = state.value, onEvent = ::handle)
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { loadTracks() }
        }
    }

    private fun handle(event: TrackListEvent) {
        when (event) {
            TrackListEvent.Back -> finish()
            is TrackListEvent.Open -> startActivity(android.content.Intent(this, TrackDetailComposeActivity::class.java).putExtra(TrackContentProvider.Schema.COL_TRACK_ID, event.id))
            is TrackListEvent.Delete -> lifecycleScope.launch(Dispatchers.IO) {
                contentResolver.delete(TrackContentProvider.CONTENT_URI_TRACK, "${TrackContentProvider.Schema.COL_TRACK_ID} = ?", arrayOf(event.id.toString()))
                loadTracks()
            }
            is TrackListEvent.Upload -> startActivity(android.content.Intent(this, OpenStreetMapUpload::class.java).putExtra(TrackContentProvider.Schema.COL_TRACK_ID, event.id))
            is TrackListEvent.Export -> requestExport(event.id)
        }
    }

    private fun requestExport(trackId: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            exportTrack(trackId)
        } else {
            pendingExportTrackId = trackId
            storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    @Suppress("DEPRECATION")
    private fun exportTrack(trackId: Long) {
        ExportToStorageTask(this, trackId).execute()
    }

    private suspend fun loadTracks() {
        val tracks = withContext(Dispatchers.IO) {
            val result = mutableListOf<TrackListItemUiState>()
            contentResolver.query(TrackContentProvider.CONTENT_URI_TRACK, null, "${TrackContentProvider.Schema.COL_ACTIVE} = ?", arrayOf("0"), "${TrackContentProvider.Schema.COL_START_DATE} DESC")?.use { cursor ->
                val id = cursor.getColumnIndex(TrackContentProvider.Schema.COL_TRACK_ID)
                val name = cursor.getColumnIndex(TrackContentProvider.Schema.COL_NAME)
                val date = cursor.getColumnIndex(TrackContentProvider.Schema.COL_START_DATE)
                val points = cursor.getColumnIndex(TrackContentProvider.Schema.COL_TRACKPOINT_COUNT)
                while (cursor.moveToNext()) result += TrackListItemUiState(
                    cursor.getLong(id), cursor.getString(name) ?: "مسیر", DateFormat.getDateTimeInstance().format(Date(cursor.getLong(date))), if (points >= 0) cursor.getInt(points) else 0,
                )
            }
            result
        }
        state.value = TrackListUiState(tracks = tracks)
    }
}
