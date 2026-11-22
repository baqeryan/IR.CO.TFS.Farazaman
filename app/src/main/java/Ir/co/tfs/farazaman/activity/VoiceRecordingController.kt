package Ir.co.tfs.farazaman.activity

import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.preference.PreferenceManager
import Ir.co.tfs.farazaman.OSMTracker
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.data.db.DataHelper
import Ir.co.tfs.farazaman.data.db.TrackContentProvider
import java.io.File
import java.util.UUID

class VoiceRecordingController(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var uuid: String? = null
    var durationSeconds: Int = 0; private set

    fun start(trackId: Long, onFinished: () -> Unit): Boolean = runCatching {
        durationSeconds = PreferenceManager.getDefaultSharedPreferences(context).getString(
            OSMTracker.Preferences.KEY_VOICEREC_DURATION, OSMTracker.Preferences.VAL_VOICEREC_DURATION,
        )?.toIntOrNull() ?: OSMTracker.Preferences.VAL_VOICEREC_DURATION.toInt()
        val directory = DataHelper.getTrackDirectory(trackId, context).apply { mkdirs() }
        check(directory.canWrite())
        val file = File(directory, DataHelper.PERSIAN_FILENAME_FORMATTER.format(saman.zamani.persiandate.PersianDate()) + DataHelper.EXTENSION_3GPP)
        uuid = UUID.randomUUID().toString()
        context.sendBroadcast(Intent(OSMTracker.INTENT_TRACK_WP).apply {
            putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId); putExtra(OSMTracker.INTENT_KEY_UUID, uuid)
            putExtra(OSMTracker.INTENT_KEY_NAME, context.getString(R.string.wpt_voicerec)); setPackage(context.packageName)
        })
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC); setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); setOutputFile(file.absolutePath); setMaxDuration(durationSeconds * 1000)
            setOnInfoListener { _, _, _ -> stop(); onFinished() }; prepare(); start()
        }
        context.sendBroadcast(Intent(OSMTracker.INTENT_UPDATE_WP).apply {
            putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId); putExtra(OSMTracker.INTENT_KEY_UUID, uuid)
            putExtra(OSMTracker.INTENT_KEY_LINK, file.name); setPackage(context.packageName)
        })
        true
    }.getOrElse { stop(); false }

    fun stop() {
        recorder?.let { runCatching { it.stop() }; runCatching { it.release() } }
        recorder = null; uuid = null
    }
}
