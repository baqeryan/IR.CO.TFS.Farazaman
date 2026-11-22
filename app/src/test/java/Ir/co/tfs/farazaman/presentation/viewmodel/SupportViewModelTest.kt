package Ir.co.tfs.farazaman.presentation.viewmodel

import Ir.co.tfs.farazaman.data.model.AppNotificationDto
import Ir.co.tfs.farazaman.presentation.compose.NotificationsMutation
import Ir.co.tfs.farazaman.presentation.compose.NotificationsReducer
import Ir.co.tfs.farazaman.presentation.compose.NotificationsUiState
import Ir.co.tfs.farazaman.presentation.compose.SupervisorProfileMutation
import Ir.co.tfs.farazaman.presentation.compose.SupervisorProfileReducer
import Ir.co.tfs.farazaman.presentation.compose.SupervisorProfileUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SupportViewModelTest {
    private fun notification(id: Int) = AppNotificationDto(
        id = id,
        title = "اعلان $id",
        createDate = "2026-09-10T10:00:00+03:30",
    )

    @Test
    fun `next page appends without duplicate notifications`() {
        val state = NotificationsUiState(items = listOf(notification(2)), loading = true)
        val result = NotificationsReducer.reduce(
            state,
            NotificationsMutation.PageLoaded(
                items = listOf(notification(2), notification(1)),
                hasMore = false,
                append = true,
            ),
        )
        assertEquals(listOf(2, 1), result.items.map { it.id })
        assertFalse(result.loading)
        assertFalse(result.hasMore)
        assertNull(result.errorMessage)
    }

    @Test
    fun `failed refresh preserves cached notifications`() {
        val state = NotificationsUiState(items = listOf(notification(1)), loading = true)
        val result = NotificationsReducer.reduce(state, NotificationsMutation.Failed("خطای شبکه"))
        assertEquals(listOf(1), result.items.map { it.id })
        assertEquals("خطای شبکه", result.errorMessage)
        assertFalse(result.loading)
    }

    @Test
    fun `deleted notification is removed locally`() {
        val state = NotificationsUiState(items = listOf(notification(2), notification(1)))
        val result = NotificationsReducer.reduce(state, NotificationsMutation.Deleted(2))
        assertEquals(listOf(1), result.items.map { it.id })
    }

    @Test
    fun `profile load controls change role permission`() {
        val initial = SupervisorProfileUiState("", null, null, canChangeRole = false)
        val result = SupervisorProfileReducer.reduce(
            initial,
            SupervisorProfileMutation.Loaded(
                displayName = "کاربر ناظر",
                userName = "supervisor",
                roleName = "ناظر قرارداد",
                canChangeRole = true,
            ),
        )
        assertEquals("کاربر ناظر", result.displayName)
        assertEquals("ناظر قرارداد", result.roleName)
        assertTrue(result.canChangeRole)
    }
}
