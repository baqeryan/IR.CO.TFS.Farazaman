package Ir.co.tfs.farazaman.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import Ir.co.tfs.farazaman.data.db.DataHelper
import Ir.co.tfs.farazaman.data.db.TrackContentProvider
import Ir.co.tfs.farazaman.presentation.compose.FaraZamanTheme
import Ir.co.tfs.farazaman.presentation.compose.track.*

class WaypointListComposeActivity : ComponentActivity() {
    private var trackId = -1L
    private val points = mutableStateOf(emptyList<WaypointUiState>())
    private var editing by mutableStateOf<WaypointUiState?>(null)
    private var editingName by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        trackId = intent.getLongExtra(TrackContentProvider.Schema.COL_TRACK_ID, -1L)
        setContent {
            FaraZamanTheme {
                WaypointListScreen(points.value, ::handleEvent)
                editing?.let { point ->
                    WaypointEditDialog(
                        name = editingName,
                        hasAttachment = !point.link.isNullOrBlank(),
                        onNameChanged = { editingName = it },
                        onSave = { update(point) },
                        onDelete = { delete(point) },
                        onPreview = { preview(point) },
                        onDismiss = { editing = null },
                    )
                }
            }
        }
        reload()
    }

    private fun handleEvent(event: WaypointEvent) {
        when (event) {
            WaypointEvent.Back -> finish()
            is WaypointEvent.OpenOnMap -> startActivity(
                Intent(this, DisplayTrackMap::class.java)
                    .putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId)
                    .putExtra(DisplayTrackMap.EXTRA_LATITUDE, event.latitude)
                    .putExtra(DisplayTrackMap.EXTRA_LONGITUDE, event.longitude)
                    .putExtra("playback_mode", true),
            )
            is WaypointEvent.Edit -> points.value.firstOrNull { it.id == event.id }?.let {
                editing = it
                editingName = it.name
            }
        }
    }

    private fun update(point: WaypointUiState) {
        DataHelper(this).updateWayPoint(trackId, point.uuid, editingName.trim(), point.link)
        editing = null
        reload()
    }

    private fun delete(point: WaypointUiState) {
        DataHelper(this).deleteWayPoint(point.uuid, attachmentPath(point))
        editing = null
        reload()
    }

    private fun preview(point: WaypointUiState) {
        val path = attachmentPath(point) ?: return
        val file = File(path)
        if (!file.exists()) return
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(this, DataHelper.FILE_PROVIDER_AUTHORITY, file)
        } else Uri.fromFile(file)
        val type = when {
            path.endsWith(DataHelper.EXTENSION_JPG) -> DataHelper.MIME_TYPE_IMAGE
            path.endsWith(DataHelper.EXTENSION_3GPP) -> DataHelper.MIME_TYPE_AUDIO
            else -> "*/*"
        }
        startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri, type).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
        editing = null
    }

    private fun attachmentPath(point: WaypointUiState): String? = point.link?.let { "${DataHelper.getTrackDirectory(trackId, this)}/$it" }

    private fun reload() {
        lifecycleScope.launch { points.value = load() }
    }

    private suspend fun load() = withContext(Dispatchers.IO) {
        val result = mutableListOf<WaypointUiState>()
        contentResolver.query(TrackContentProvider.waypointsUri(trackId), null, null, null, "${TrackContentProvider.Schema.COL_TIMESTAMP} desc")?.use { cursor ->
            val id = cursor.getColumnIndex(TrackContentProvider.Schema.COL_ID)
            val name = cursor.getColumnIndex(TrackContentProvider.Schema.COL_NAME)
            val description = cursor.getColumnIndex(TrackContentProvider.Schema.COL_DESCRIPTION)
            val lat = cursor.getColumnIndex(TrackContentProvider.Schema.COL_LATITUDE)
            val lon = cursor.getColumnIndex(TrackContentProvider.Schema.COL_LONGITUDE)
            val uuid = cursor.getColumnIndex(TrackContentProvider.Schema.COL_UUID)
            val link = cursor.getColumnIndex(TrackContentProvider.Schema.COL_LINK)
            while (cursor.moveToNext()) result += WaypointUiState(
                cursor.getLong(id),
                if (name >= 0) cursor.getString(name) ?: "نقطه" else "نقطه",
                if (description >= 0) cursor.getString(description) ?: "" else "",
                cursor.getDouble(lat),
                cursor.getDouble(lon),
                if (uuid >= 0) cursor.getString(uuid).orEmpty() else "",
                if (link >= 0) cursor.getString(link) else null,
            )
        }
        result
    }
}
