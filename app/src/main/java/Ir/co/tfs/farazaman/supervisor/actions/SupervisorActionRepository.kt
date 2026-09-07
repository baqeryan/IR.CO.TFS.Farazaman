package Ir.co.tfs.farazaman.supervisor.actions

import android.content.ContentUris
import android.content.Context
import android.preference.PreferenceManager
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.data.db.TrackContentProvider
import Ir.co.tfs.farazaman.supervisor.SupervisorViolationHistoryStore
import java.io.File

class SupervisorActionRepository(context: Context) {
    private val appContext = context.applicationContext

    fun categories(): List<UserActionCategory> = listOf(
        UserActionCategory(UserActionCategories.ALL, appContext.getString(R.string.action_filter_all)),
        UserActionCategory(UserActionCategories.MISSIONS, appContext.getString(R.string.action_filter_missions)),
        UserActionCategory(UserActionCategories.PENALTIES, appContext.getString(R.string.action_filter_penalties)),
        UserActionCategory(UserActionCategories.UPLOADS, appContext.getString(R.string.action_filter_uploads)),
        UserActionCategory(UserActionCategories.PAUSES, appContext.getString(R.string.action_filter_pauses)),
        UserActionCategory(UserActionCategories.OTHER, appContext.getString(R.string.action_filter_other)),
    )

    fun loadPage(filter: String, offset: Int, limit: Int): UserActionPage {
        ensureExistingDataImported()
        val all = UserActionStore.load(appContext)
        return UserActionFeedPolicy.preparePage(
            records = all,
            filter = filter,
            offset = offset,
            limit = limit,
            isMissionPaused = ::isMissionPaused,
            fileExists = { path -> File(path).let { it.isFile && it.length() > 0L } },
        )
    }

    private fun ensureExistingDataImported() {
        if (UserActionStore.isBootstrapComplete(appContext)) return

        SupervisorViolationHistoryStore.loadAll(appContext).forEach { record ->
            SupervisorActionRecorder.recordPenalty(
                context = appContext,
                historyRecordId = record.id,
                trackId = record.trackId,
                violationId = record.violationId,
                violationTitle = record.title,
                violationSubtitle = record.subtitle,
                timestampMs = record.timestampMs,
            )
        }

        val selection = "${TrackContentProvider.Schema.TBL_TRACK}.${TrackContentProvider.Schema.COL_ROLE} = ?"
        appContext.contentResolver.query(
            TrackContentProvider.CONTENT_URI_TRACK,
            null,
            selection,
            arrayOf("supervisor"),
            "${TrackContentProvider.Schema.TBL_TRACK}.${TrackContentProvider.Schema.COL_START_DATE} DESC",
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(TrackContentProvider.Schema.COL_ID)
            val startColumn = cursor.getColumnIndexOrThrow(TrackContentProvider.Schema.COL_START_DATE)
            val activeColumn = cursor.getColumnIndexOrThrow(TrackContentProvider.Schema.COL_ACTIVE)
            val pointsColumn = cursor.getColumnIndexOrThrow(TrackContentProvider.Schema.COL_TRACKPOINT_COUNT)
            while (cursor.moveToNext()) {
                val trackId = cursor.getLong(idColumn)
                if (!hasMissionData(trackId)) continue
                val createdAt = cursor.getLong(startColumn)
                val pointCount = cursor.getInt(pointsColumn)
                val active = cursor.getInt(activeColumn) == TrackContentProvider.Schema.VAL_TRACK_ACTIVE
                SupervisorActionRecorder.recordMissionCreated(appContext, trackId, createdAt)
                if (pointCount > 0) {
                    val firstPointAt = pointTimestamp(trackId, first = true) ?: createdAt
                    SupervisorActionRecorder.recordMissionStarted(
                        appContext,
                        trackId,
                        resumed = false,
                        timestampMs = firstPointAt,
                    )
                    if (!active) {
                        val pausedAt = pointTimestamp(trackId, first = false) ?: firstPointAt
                        SupervisorActionRecorder.recordMissionPaused(appContext, trackId, pausedAt)
                    }
                }
            }
        }
        UserActionStore.markBootstrapComplete(appContext)
    }

    private fun isMissionPaused(trackId: Long): Boolean {
        if (!hasMissionData(trackId)) return false
        val uri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId)
        return appContext.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) return@use false
            val active = cursor.getInt(cursor.getColumnIndexOrThrow(TrackContentProvider.Schema.COL_ACTIVE))
            val pointCount = cursor.getInt(cursor.getColumnIndexOrThrow(TrackContentProvider.Schema.COL_TRACKPOINT_COUNT))
            active == TrackContentProvider.Schema.VAL_TRACK_INACTIVE && pointCount > 0
        } ?: false
    }

    private fun hasMissionData(trackId: Long): Boolean =
        PreferenceManager.getDefaultSharedPreferences(appContext)
            .getString("mission_data_$trackId", null) != null

    private fun pointTimestamp(trackId: Long, first: Boolean): Long? {
        val uri = if (first) {
            TrackContentProvider.trackStartUri(trackId)
        } else {
            TrackContentProvider.trackEndUri(trackId)
        }
        return appContext.contentResolver.query(
            uri,
            arrayOf(TrackContentProvider.Schema.COL_TIMESTAMP),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getLong(cursor.getColumnIndexOrThrow(TrackContentProvider.Schema.COL_TIMESTAMP))
            } else {
                null
            }
        }
    }
}
