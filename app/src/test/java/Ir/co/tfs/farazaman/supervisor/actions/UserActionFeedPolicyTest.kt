package Ir.co.tfs.farazaman.supervisor.actions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserActionFeedPolicyTest {

    @Test
    fun `only latest effective pause exposes resume`() {
        val oldPause = missionRecord(
            id = "old-pause",
            type = UserActionTypes.MISSION_PAUSED,
            timestamp = 100,
            operations = listOf(UserActionOperations.RESUME_MISSION),
        )
        val resumed = missionRecord(
            id = "resumed",
            type = UserActionTypes.MISSION_RESUMED,
            timestamp = 200,
        )

        val page = page(listOf(oldPause, resumed), missionPaused = true)

        assertTrue(page.items.first { it.id == "old-pause" }.operationKeys.isEmpty())
    }

    @Test
    fun `latest paused mission exposes resume when persisted state is paused`() {
        val pause = missionRecord(
            id = "pause",
            type = UserActionTypes.MISSION_PAUSED,
            timestamp = 200,
            operations = listOf(UserActionOperations.RESUME_MISSION),
        )

        val page = page(listOf(pause), missionPaused = true)

        assertEquals(listOf(UserActionOperations.RESUME_MISSION), page.items.single().operationKeys)
    }

    @Test
    fun `gpx operation is removed when file is unavailable`() {
        val completed = missionRecord(
            id = "completed",
            type = UserActionTypes.MISSION_COMPLETED,
            timestamp = 300,
            operations = listOf(UserActionOperations.OPEN_GPX),
        ).copy(metadata = mapOf(UserActionMetadataKeys.GPX_PATH to "missing.gpx"))

        val page = UserActionFeedPolicy.preparePage(
            records = listOf(completed),
            filter = UserActionCategories.ALL,
            offset = 0,
            limit = 30,
            isMissionPaused = { false },
            fileExists = { false },
        )

        assertTrue(page.items.single().operationKeys.isEmpty())
    }

    @Test
    fun `filter and pagination keep newest actions first`() {
        val records = listOf(
            missionRecord("one", UserActionTypes.MISSION_STARTED, 100),
            missionRecord("two", UserActionTypes.MISSION_PAUSED, 300).copy(category = UserActionCategories.PAUSES),
            missionRecord("three", UserActionTypes.MISSION_STARTED, 200),
        )

        val all = page(records, missionPaused = false, limit = 2)
        val pauses = UserActionFeedPolicy.preparePage(
            records,
            UserActionCategories.PAUSES,
            0,
            30,
            isMissionPaused = { false },
            fileExists = { false },
        )

        assertEquals(listOf("two", "three"), all.items.map { it.id })
        assertTrue(all.hasMore)
        assertEquals(listOf("two"), pauses.items.map { it.id })
        assertFalse(pauses.hasMore)
    }

    private fun page(
        records: List<UserActionRecord>,
        missionPaused: Boolean,
        limit: Int = 30,
    ) = UserActionFeedPolicy.preparePage(
        records = records,
        filter = UserActionCategories.ALL,
        offset = 0,
        limit = limit,
        isMissionPaused = { missionPaused },
        fileExists = { true },
    )

    private fun missionRecord(
        id: String,
        type: String,
        timestamp: Long,
        operations: List<String> = emptyList(),
    ) = UserActionRecord(
        id = id,
        type = type,
        title = id,
        description = "",
        timestampMs = timestamp,
        category = UserActionCategories.MISSIONS,
        relatedEntityType = UserActionEntities.MISSION,
        relatedEntityId = "42",
        operationKeys = operations,
    )
}
