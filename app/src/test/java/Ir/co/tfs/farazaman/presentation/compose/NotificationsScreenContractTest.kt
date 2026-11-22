package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.data.model.AppNotificationDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationsScreenContractTest {
    @Test fun emptyStateDefaultsToEmptyAndLoadingFalse() {
        val state = NotificationsUiState()
        assertTrue(state.items.isEmpty())
        assertTrue(!state.loading)
        assertTrue(state.hasMore)
    }

    @Test fun eventsCarryNotificationIdentity() {
        val item = AppNotificationDto(125, "اعلان", createDate = "2026-09-08")
        assertEquals(125, (NotificationsUiEvent.Open(item) as NotificationsUiEvent.Open).notification.id)
        assertEquals(125, (NotificationsUiEvent.Delete(item) as NotificationsUiEvent.Delete).notification.id)
    }
}
