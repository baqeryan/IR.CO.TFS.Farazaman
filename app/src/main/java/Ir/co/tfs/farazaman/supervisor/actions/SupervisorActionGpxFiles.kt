package Ir.co.tfs.farazaman.supervisor.actions

import android.content.Context
import java.io.File

object SupervisorActionGpxFiles {
    private const val DIRECTORY = "action_gpx"

    fun persist(context: Context, trackId: Long, source: File, timestampMs: Long): File? {
        if (!source.isFile || source.length() == 0L) return null
        return runCatching {
            val directory = File(context.filesDir, DIRECTORY)
            check(directory.exists() || directory.mkdirs())
            File(directory, "mission_${trackId}_$timestampMs.gpx").also { target ->
                source.copyTo(target, overwrite = true)
            }
        }.getOrNull()
    }
}
