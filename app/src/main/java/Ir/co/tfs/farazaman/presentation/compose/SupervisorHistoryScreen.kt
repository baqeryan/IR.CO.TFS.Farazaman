package Ir.co.tfs.farazaman.presentation.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import Ir.co.tfs.farazaman.presentation.design.components.*
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens

data class SupervisorHistoryDateUiItem(val id: String, val weekday: String, val dayNumber: String, val selected: Boolean, val holiday: Boolean = false)
data class SupervisorHistoryBadgeUiItem(val label: String, val variant: FzStatusVariant)
data class SupervisorHistoryEntryUiItem(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val dateTimes: List<String> = emptyList(),
    val badges: List<SupervisorHistoryBadgeUiItem> = emptyList(),
)
data class SupervisorHistoryUiState(
    val dates: List<SupervisorHistoryDateUiItem> = emptyList(),
    val items: List<SupervisorHistoryEntryUiItem> = emptyList(),
    val unreadNotifications: Int = 0,
)
sealed interface SupervisorHistoryEvent {
    data object OpenNotifications : SupervisorHistoryEvent
    data object OpenProfile : SupervisorHistoryEvent
    data class SelectDate(val id: String) : SupervisorHistoryEvent
}

@Composable
fun SupervisorHistoryScreen(
    state: SupervisorHistoryUiState,
    onEvent: (SupervisorHistoryEvent) -> Unit,
    modifier: Modifier = Modifier,
    bottomContentPadding: Dp = 0.dp,
) {
    val spacing = FaraZamanThemeTokens.spacing
    FzPageTemplate(
        title = "تاریخچه",
        headerSize = FzHeaderSize.Compact,
        headerStart = {
            SupervisorNotificationAction(
                unreadCount = state.unreadNotifications,
                onClick = { onEvent(SupervisorHistoryEvent.OpenNotifications) },
            )
        },
        headerEnd = { SupervisorProfileAction(onClick = { onEvent(SupervisorHistoryEvent.OpenProfile) }) },
        modifier = modifier,
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.spacedBy(spacing.md)) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm, Alignment.CenterHorizontally),
        ) {
            items(state.dates, key = { it.id }) { date ->
                FzCard(
                    variant = if (date.selected) FzCardVariant.Selected else FzCardVariant.Outlined,
                    onClick = { onEvent(SupervisorHistoryEvent.SelectDate(date.id)) },
                ) {
                    Column(
                        Modifier.widthIn(min = FaraZamanThemeTokens.sizes.minimumTouchTarget)
                            .padding(horizontal = spacing.md, vertical = spacing.sm),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(date.weekday, style = MaterialTheme.typography.labelSmall)
                        Text(date.dayNumber, style = MaterialTheme.typography.titleMedium, color = if (date.holiday) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
        if (state.items.isEmpty()) {
            FzEmptyState("موردی ثبت نشد", "برای روز انتخاب‌شده سابقه‌ای وجود ندارد.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
                contentPadding = PaddingValues(bottom = bottomContentPadding + spacing.sm),
            ) {
                items(state.items, key = { it.id }) { item ->
                    FzCard(Modifier.fillMaxWidth(), variant = FzCardVariant.Outlined) {
                        Column(Modifier.fillMaxWidth().padding(spacing.md), verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                            Text(item.title, style = MaterialTheme.typography.titleMedium)
                            if (item.subtitle.isNotBlank()) Text(item.subtitle, style = MaterialTheme.typography.bodyMedium)
                            item.dateTimes.forEach { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                                item.badges.forEach { badge -> FzStatusChip(badge.label, variant = badge.variant) }
                            }
                        }
                    }
                }
            }
        }
        }
    }
}
