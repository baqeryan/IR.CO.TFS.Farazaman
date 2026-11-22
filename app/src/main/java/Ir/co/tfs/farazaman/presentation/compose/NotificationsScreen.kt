package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.data.model.AppNotificationDto
import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzButtonVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzCard
import Ir.co.tfs.farazaman.presentation.design.components.FzEmptyState
import Ir.co.tfs.farazaman.presentation.design.components.FzLoading
import Ir.co.tfs.farazaman.presentation.design.components.FzScaffold
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class NotificationsUiState(
    val items: List<AppNotificationDto> = emptyList(),
    val loading: Boolean = false,
    val hasMore: Boolean = true,
    val errorMessage: String? = null,
)

sealed interface NotificationsUiEvent {
    data object Retry : NotificationsUiEvent
    data object LoadNextPage : NotificationsUiEvent
    data class Open(val notification: AppNotificationDto) : NotificationsUiEvent
    data class Delete(val notification: AppNotificationDto) : NotificationsUiEvent
}

sealed interface NotificationsMutation {
    data object Loading : NotificationsMutation
    data class PageLoaded(
        val items: List<AppNotificationDto>,
        val hasMore: Boolean,
        val append: Boolean,
    ) : NotificationsMutation
    data class Failed(val message: String) : NotificationsMutation
    data class Deleted(val id: Int) : NotificationsMutation
}

object NotificationsReducer {
    fun reduce(state: NotificationsUiState, mutation: NotificationsMutation) = when (mutation) {
        NotificationsMutation.Loading -> state.copy(loading = true, errorMessage = null)
        is NotificationsMutation.PageLoaded -> state.copy(
            items = if (mutation.append) (state.items + mutation.items).distinctBy { it.id } else mutation.items,
            loading = false,
            hasMore = mutation.hasMore,
            errorMessage = null,
        )
        is NotificationsMutation.Failed -> state.copy(loading = false, errorMessage = mutation.message)
        is NotificationsMutation.Deleted -> state.copy(items = state.items.filterNot { it.id == mutation.id })
    }
}

@Composable
fun NotificationsScreen(
    state: NotificationsUiState,
    onEvent: (NotificationsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FaraZamanThemeTokens.spacing
    FzScaffold(modifier = modifier) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            when {
                state.loading && state.items.isEmpty() -> FzLoading("در حال دریافت اعلان‌ها...")
                state.errorMessage != null && state.items.isEmpty() -> {
                    FzEmptyState("خطا در دریافت اعلان‌ها", state.errorMessage)
                    FzButton("تلاش مجدد", { onEvent(NotificationsUiEvent.Retry) }, modifier = Modifier.fillMaxWidth())
                }
                state.items.isEmpty() -> FzEmptyState("اعلانی وجود ندارد", "اعلان جدیدی برای نمایش ثبت نشده است.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    items(state.items, key = { it.id }) { item ->
                        NotificationCard(item, { onEvent(NotificationsUiEvent.Open(item)) }, { onEvent(NotificationsUiEvent.Delete(item)) })
                    }
                    if (state.hasMore) item { FzButton("دریافت موارد بیشتر", { onEvent(NotificationsUiEvent.LoadNextPage) }, variant = FzButtonVariant.Ghost, modifier = Modifier.fillMaxWidth()) }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(item: AppNotificationDto, onOpen: () -> Unit, onDelete: () -> Unit) {
    val spacing = FaraZamanThemeTokens.spacing
    FzCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(spacing.md), verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium)
            Text(item.createDate, style = MaterialTheme.typography.bodySmall)
            FzButton(if (item.isRead) "مشاهده" else "مشاهده (جدید)", onOpen, modifier = Modifier.fillMaxWidth())
            FzButton("حذف", onDelete, variant = FzButtonVariant.Danger, modifier = Modifier.fillMaxWidth())
        }
    }
}
