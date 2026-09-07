package Ir.co.tfs.farazaman.supervisor.actions

/**
 * String keys intentionally keep the feed open to new server/local action types without
 * requiring the UI model to become a closed enum.
 */
object UserActionTypes {
    const val MISSION_CREATED = "mission_created"
    const val MISSION_STARTED = "mission_started"
    const val MISSION_RESUMED = "mission_resumed"
    const val MISSION_PAUSED = "mission_paused"
    const val MISSION_COMPLETED = "mission_completed"
    const val MISSION_CANCELLED = "mission_cancelled"
    const val PENALTY_RECORDED = "penalty_recorded"
}

object UserActionCategories {
    const val ALL = "all"
    const val MISSIONS = "missions"
    const val PENALTIES = "penalties"
    const val UPLOADS = "uploads"
    const val PAUSES = "pauses"
    const val OTHER = "other"

    val known = setOf(MISSIONS, PENALTIES, UPLOADS, PAUSES)
}

object UserActionOperations {
    const val RESUME_MISSION = "resume_mission"
    const val OPEN_GPX = "open_gpx"
}

object UserActionEntities {
    const val MISSION = "mission"
    const val VIOLATION = "violation"
}

object UserActionMetadataKeys {
    const val ORGANIZATION = "organization"
    const val CONTRACT = "contract"
    const val LOCATION = "location"
    const val RECORD_ID = "record_id"
    const val MISSION_ID = "mission_id"
    const val MISSION_START_AT = "mission_start_at"
    const val PAUSED_AT = "paused_at"
    const val FINISHED_AT = "finished_at"
    const val UPLOADED_AT = "uploaded_at"
    const val DURATION_MS = "duration_ms"
    const val GPX_PATH = "gpx_path"
    const val LATITUDE = "latitude"
    const val LONGITUDE = "longitude"
}

data class UserActionRecord(
    val id: String,
    val type: String,
    val title: String,
    val description: String,
    val timestampMs: Long,
    val category: String,
    val iconKey: String? = null,
    val status: String? = null,
    val relatedEntityType: String? = null,
    val relatedEntityId: String? = null,
    val metadata: Map<String, String> = emptyMap(),
    val operationKeys: List<String> = emptyList(),
)

data class UserActionCategory(
    val key: String,
    val title: String,
)

data class UserActionPage(
    val items: List<UserActionRecord>,
    val hasMore: Boolean,
)
