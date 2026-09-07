package Ir.co.tfs.farazaman.supervisor.actions

object UserActionFeedPolicy {
    fun preparePage(
        records: List<UserActionRecord>,
        filter: String,
        offset: Int,
        limit: Int,
        isMissionPaused: (Long) -> Boolean,
        fileExists: (String) -> Boolean,
    ): UserActionPage {
        val sorted = records.sortedByDescending { it.timestampMs }
        val latestMissionActionIds = sorted.asSequence()
            .filter { it.relatedEntityType == UserActionEntities.MISSION }
            .groupBy { it.relatedEntityId }
            .mapValues { (_, actions) -> actions.first().id }

        val available = sorted.map { record ->
            record.copy(
                operationKeys = record.operationKeys.filter { operation ->
                    when (operation) {
                        UserActionOperations.RESUME_MISSION -> {
                            latestMissionActionIds[record.relatedEntityId] == record.id &&
                                record.relatedEntityId?.toLongOrNull()?.let(isMissionPaused) == true
                        }
                        UserActionOperations.OPEN_GPX ->
                            record.metadata[UserActionMetadataKeys.GPX_PATH]?.let(fileExists) == true
                        else -> false
                    }
                },
            )
        }
        val filtered = when (filter) {
            UserActionCategories.ALL -> available
            UserActionCategories.OTHER -> available.filter { it.category !in UserActionCategories.known }
            else -> available.filter { it.category == filter }
        }
        val safeOffset = offset.coerceAtLeast(0)
        val pageItems = filtered.drop(safeOffset).take(limit.coerceAtLeast(1))
        return UserActionPage(
            items = pageItems,
            hasMore = safeOffset + pageItems.size < filtered.size,
        )
    }
}
