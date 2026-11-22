package Ir.co.tfs.farazaman.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import Ir.co.tfs.farazaman.data.db.TrackContentProvider
import Ir.co.tfs.farazaman.gpx.ExportToStorageTask
import Ir.co.tfs.farazaman.presentation.compose.FaraZamanTheme
import Ir.co.tfs.farazaman.presentation.compose.track.TrackDetailEvent
import Ir.co.tfs.farazaman.presentation.compose.track.TrackDetailScreen
import Ir.co.tfs.farazaman.presentation.compose.track.TrackDetailUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrackDetailComposeActivity : ComponentActivity() {
    private var trackId: Long = -1L
    private val storagePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) exportTrack()
    }
    private val state = mutableStateOf(TrackDetailUiState(-1L, "مسیر", "", 0, 0))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        trackId = intent.getLongExtra(TrackContentProvider.Schema.COL_TRACK_ID, -1L)
        setContent { FaraZamanTheme { TrackDetailScreen(state = state.value, onEvent = ::handle) } }
        lifecycleScope.launch { loadTrack() }
    }

    private fun handle(event: TrackDetailEvent) {
        when (event) {
            TrackDetailEvent.Back -> finish()
            TrackDetailEvent.OpenMap -> startActivity(Intent(this, DisplayTrackMap::class.java).putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId).putExtra("playback_mode", true))
            TrackDetailEvent.AddWaypoint -> startActivity(Intent(this, WaypointListComposeActivity::class.java).putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId))
            TrackDetailEvent.Export -> requestExport()
            TrackDetailEvent.Upload -> startActivity(Intent(this, OpenStreetMapUpload::class.java).putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId))
        }
    }

    private fun requestExport() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            exportTrack()
        } else storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    @Suppress("DEPRECATION")
    private fun exportTrack() {
        ExportToStorageTask(this, trackId).execute()
    }

    private suspend fun loadTrack() {
        val loaded = withContext(Dispatchers.IO) {
            var title = "مسیر"
            var date = ""
            var points = 0
            var waypoints = 0
            contentResolver.query(TrackContentProvider.CONTENT_URI_TRACK, null, "${TrackContentProvider.Schema.COL_TRACK_ID} = ?", arrayOf(trackId.toString()), null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getColumnIndex(TrackContentProvider.Schema.COL_NAME).takeIf { it >= 0 }?.let { title = cursor.getString(it) ?: title }
                    cursor.getColumnIndex(TrackContentProvider.Schema.COL_START_DATE).takeIf { it >= 0 }?.let { date = cursor.getLong(it).toString() }
                    cursor.getColumnIndex(TrackContentProvider.Schema.COL_TRACKPOINT_COUNT).takeIf { it >= 0 }?.let { points = cursor.getInt(it) }
                    cursor.getColumnIndex(TrackContentProvider.Schema.COL_WAYPOINT_COUNT).takeIf { it >= 0 }?.let { waypoints = cursor.getInt(it) }
                }
            }
            TrackDetailUiState(trackId, title, date, points, waypoints)
        }
        state.value = loaded
    }
}
