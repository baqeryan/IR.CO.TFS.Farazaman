package Ir.co.tfs.farazaman.supervisor.actions

import android.content.Context
import android.preference.PreferenceManager
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.data.db.TrackContentProvider
import org.json.JSONObject
import java.io.File
import java.util.UUID

object SupervisorActionRecorder {

    fun recordMissionCreated(
        context: Context,
        trackId: Long,
        timestampMs: Long,
        missionData: Map<String, Any?>? = null,
    ) {
        val details = missionDetails(context, trackId, missionData)
        UserActionStore.upsert(
            context,
            UserActionRecord(
                id = "mission:$trackId:created",
                type = UserActionTypes.MISSION_CREATED,
                title = context.getString(R.string.action_mission_received),
                description = details.description(context),
                timestampMs = timestampMs,
                category = UserActionCategories.MISSIONS,
                iconKey = "mission",
                status = context.getString(R.string.action_status_ready),
                relatedEntityType = UserActionEntities.MISSION,
                relatedEntityId = trackId.toString(),
                metadata = details.metadata(),
            ),
        )
    }

    fun recordMissionStarted(
        context: Context,
        trackId: Long,
        resumed: Boolean,
        timestampMs: Long = System.currentTimeMillis(),
    ) {
        val timestamp = timestampMs
        val details = missionDetails(context, trackId)
        UserActionStore.upsert(
            context,
            UserActionRecord(
                id = if (resumed) "mission:$trackId:resumed:$timestamp" else "mission:$trackId:started",
                type = if (resumed) UserActionTypes.MISSION_RESUMED else UserActionTypes.MISSION_STARTED,
                title = context.getString(
                    if (resumed) R.string.action_mission_resumed else R.string.action_mission_started,
                ),
                description = details.description(context),
                timestampMs = timestamp,
                category = UserActionCategories.MISSIONS,
                iconKey = "mission",
                status = context.getString(R.string.action_status_in_progress),
                relatedEntityType = UserActionEntities.MISSION,
                relatedEntityId = trackId.toString(),
                metadata = details.metadata() +
                    (UserActionMetadataKeys.MISSION_START_AT to
                        if (resumed) missionStartedAt(context, trackId, timestamp).toString()
                        else timestamp.toString()),
            ),
        )
    }

    fun recordMissionPaused(
        context: Context,
        trackId: Long,
        timestampMs: Long = System.currentTimeMillis(),
    ) {
        val timestamp = timestampMs
        val details = missionDetails(context, trackId)
        UserActionStore.upsert(
            context,
            UserActionRecord(
                id = "mission:$trackId:paused:$timestamp",
                type = UserActionTypes.MISSION_PAUSED,
                title = context.getString(R.string.action_mission_paused),
                description = details.description(context),
                timestampMs = timestamp,
                category = UserActionCategories.PAUSES,
                iconKey = "pause",
                status = context.getString(R.string.action_status_paused),
                relatedEntityType = UserActionEntities.MISSION,
                relatedEntityId = trackId.toString(),
                metadata = details.metadata() + mapOf(
                    UserActionMetadataKeys.MISSION_START_AT to missionStartedAt(context, trackId, timestamp).toString(),
                    UserActionMetadataKeys.PAUSED_AT to timestamp.toString(),
                ),
                operationKeys = listOf(UserActionOperations.RESUME_MISSION),
            ),
        )
    }

    fun recordMissionCompleted(
        context: Context,
        trackId: Long,
        completedAt: Long,
        gpxFile: File?,
    ) {
        val details = missionDetails(context, trackId)
        val startAt = missionStartedAt(context, trackId, completedAt)
        val metadata = details.metadata().toMutableMap().apply {
            put(UserActionMetadataKeys.MISSION_START_AT, startAt.toString())
            put(UserActionMetadataKeys.FINISHED_AT, completedAt.toString())
            put(UserActionMetadataKeys.UPLOADED_AT, completedAt.toString())
            put(UserActionMetadataKeys.DURATION_MS, (completedAt - startAt).coerceAtLeast(0L).toString())
            gpxFile?.absolutePath?.let { put(UserActionMetadataKeys.GPX_PATH, it) }
        }
        UserActionStore.upsert(
            context,
            UserActionRecord(
                id = "mission:$trackId:completed",
                type = UserActionTypes.MISSION_COMPLETED,
                title = context.getString(R.string.action_mission_completed),
                description = details.description(context),
                timestampMs = completedAt,
                category = UserActionCategories.UPLOADS,
                iconKey = "upload",
                status = context.getString(R.string.action_status_uploaded),
                relatedEntityType = UserActionEntities.MISSION,
                relatedEntityId = trackId.toString(),
                metadata = metadata,
                operationKeys = if (gpxFile != null) listOf(UserActionOperations.OPEN_GPX) else emptyList(),
            ),
        )
    }

    fun recordMissionCancelled(context: Context, trackId: Long) {
        val timestamp = System.currentTimeMillis()
        val details = missionDetails(context, trackId)
        UserActionStore.upsert(
            context,
            UserActionRecord(
                id = "mission:$trackId:cancelled",
                type = UserActionTypes.MISSION_CANCELLED,
                title = context.getString(R.string.action_mission_cancelled),
                description = details.description(context),
                timestampMs = timestamp,
                category = UserActionCategories.MISSIONS,
                iconKey = "mission",
                status = context.getString(R.string.action_status_cancelled),
                relatedEntityType = UserActionEntities.MISSION,
                relatedEntityId = trackId.toString(),
                metadata = details.metadata(),
            ),
        )
    }

    fun recordPenalty(
        context: Context,
        historyRecordId: String,
        trackId: Long,
        violationId: String,
        violationTitle: String,
        violationSubtitle: String,
        locationName: String = "",
        organization: String = "",
        contract: String = "",
        latitude: Double? = null,
        longitude: Double? = null,
        timestampMs: Long = System.currentTimeMillis(),
    ) {
        val normalizedLocation = locationName.trim()
        val title = if (normalizedLocation.isNotEmpty()) {
            context.getString(R.string.action_penalty_recorded_at, normalizedLocation)
        } else {
            context.getString(R.string.action_penalty_recorded)
        }
        val description = listOf(violationTitle.trim(), violationSubtitle.trim())
            .filter { it.isNotEmpty() }
            .joinToString(" • ")
        val metadata = buildMap {
            put(UserActionMetadataKeys.MISSION_ID, trackId.toString())
            normalizedLocation.takeIf { it.isNotEmpty() }?.let {
                put(UserActionMetadataKeys.LOCATION, it)
            }
            organization.trim().takeIf { it.isNotEmpty() }?.let {
                put(UserActionMetadataKeys.ORGANIZATION, it)
            }
            contract.trim().takeIf { it.isNotEmpty() }?.let {
                put(UserActionMetadataKeys.CONTRACT, it)
            }
            violationId.trim().takeIf { it.isNotEmpty() }?.let {
                put(UserActionMetadataKeys.RECORD_ID, it)
            }
            latitude?.takeIf { it in -90.0..90.0 }?.let {
                put(UserActionMetadataKeys.LATITUDE, it.toString())
            }
            longitude?.takeIf { it in -180.0..180.0 }?.let {
                put(UserActionMetadataKeys.LONGITUDE, it.toString())
            }
        }
        UserActionStore.upsert(
            context,
            UserActionRecord(
                id = "violation:${historyRecordId.ifBlank { UUID.randomUUID().toString() }}",
                type = UserActionTypes.PENALTY_RECORDED,
                title = title,
                description = description,
                timestampMs = timestampMs,
                category = UserActionCategories.PENALTIES,
                iconKey = "penalty",
                status = context.getString(R.string.action_status_registered),
                relatedEntityType = UserActionEntities.VIOLATION,
                relatedEntityId = violationId.ifBlank { historyRecordId },
                metadata = metadata,
            ),
        )
    }

    private fun missionStartedAt(context: Context, trackId: Long, fallback: Long): Long {
        UserActionStore.load(context)
            .asSequence()
            .filter {
                it.type == UserActionTypes.MISSION_STARTED &&
                    it.relatedEntityType == UserActionEntities.MISSION &&
                    it.relatedEntityId == trackId.toString()
            }
            .minOfOrNull { it.timestampMs }
            ?.takeIf { it > 0L }
            ?.let { return it }

        return context.contentResolver.query(
            TrackContentProvider.trackStartUri(trackId),
            arrayOf(TrackContentProvider.Schema.COL_TIMESTAMP),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getLong(cursor.getColumnIndexOrThrow(TrackContentProvider.Schema.COL_TIMESTAMP))
            } else {
                fallback
            }
        } ?: fallback
    }

    private fun missionDetails(
        context: Context,
        trackId: Long,
        supplied: Map<String, Any?>? = null,
    ): MissionDetails {
        val stored = PreferenceManager.getDefaultSharedPreferences(context)
            .getString("mission_data_$trackId", null)
        val json = runCatching { stored?.let(::JSONObject) }.getOrNull()
        return MissionDetails(
            organization = supplied?.get("organTitle")?.toString()
                ?: json?.optString("organTitle").orEmpty(),
            contract = supplied?.get("contractTitle")?.toString()
                ?: json?.optString("contractTitle").orEmpty(),
        )
    }

    private data class MissionDetails(
        val organization: String,
        val contract: String,
    ) {
        fun description(context: Context): String = when {
            organization.isNotBlank() && contract.isNotBlank() ->
                context.getString(R.string.action_mission_context, organization, contract)
            organization.isNotBlank() -> organization
            contract.isNotBlank() -> contract
            else -> ""
        }

        fun metadata(): Map<String, String> = buildMap {
            organization.trim().takeIf { it.isNotEmpty() }?.let {
                put(UserActionMetadataKeys.ORGANIZATION, it)
            }
            contract.trim().takeIf { it.isNotEmpty() }?.let {
                put(UserActionMetadataKeys.CONTRACT, it)
            }
        }
    }
}
