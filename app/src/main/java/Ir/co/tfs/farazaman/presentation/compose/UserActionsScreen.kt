package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzButtonVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzCard
import Ir.co.tfs.farazaman.presentation.design.components.FzChipItem
import Ir.co.tfs.farazaman.presentation.design.components.FzChipSet
import Ir.co.tfs.farazaman.presentation.design.components.FzEmptyState
import Ir.co.tfs.farazaman.presentation.design.components.FzLoading
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import Ir.co.tfs.farazaman.presentation.design.theme.FzComponentSize
import Ir.co.tfs.farazaman.presentation.design.theme.FzSelectionMode
import Ir.co.tfs.farazaman.supervisor.actions.UserActionCategory
import Ir.co.tfs.farazaman.supervisor.actions.UserActionOperations
import Ir.co.tfs.farazaman.supervisor.actions.UserActionRecord
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

data class UserActionsUiState(
    val categories: List<UserActionCategory> = emptyList(),
    val items: List<UserActionRecord> = emptyList(),
    val selectedFilter: String = "all",
    val loading: Boolean = false,
    val hasMore: Boolean = false,
)

sealed interface UserActionsUiEvent : UiEvent {
    data class SelectFilter(val key: String) : UserActionsUiEvent
    data class RunOperation(val item: UserActionRecord, val operation: String) : UserActionsUiEvent
    data object LoadNextPage : UserActionsUiEvent
}

@Composable
fun UserActionsScreen(
    state: UserActionsUiState,
    onEvent: (UserActionsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FaraZamanThemeTokens.spacing
    val listState = rememberLazyListState()
    val reachedEnd by remember(state.items.size, state.hasMore) {
        derivedStateOf {
            state.hasMore && listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == state.items.lastIndex
        }
    }
    LaunchedEffect(reachedEnd) { if (reachedEnd) onEvent(UserActionsUiEvent.LoadNextPage) }

    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        FzChipSet(
            items = state.categories.map { category ->
                FzChipItem(value = category.key, label = category.title)
            },
            selectedValues = setOf(state.selectedFilter),
            onSelectionChange = { selection ->
                selection.firstOrNull()?.let { key ->
                    onEvent(UserActionsUiEvent.SelectFilter(key))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            mode = FzSelectionMode.Single,
            size = FzComponentSize.Small,
        )
        when {
            state.loading && state.items.isEmpty() -> FzLoading("در حال دریافت فعالیت‌ها...")
            state.items.isEmpty() -> FzEmptyState("فعالیتی ثبت نشده است")
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                items(state.items, key = { it.id }) { item ->
                    FzCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(spacing.md), verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                            Text(item.title, style = MaterialTheme.typography.titleMedium)
                            if (item.description.isNotBlank()) Text(item.description, style = MaterialTheme.typography.bodyMedium)
                            item.status?.takeIf(String::isNotBlank)?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                            item.operationKeys.forEach { operation ->
                                FzButton(
                                    text = when (operation) {
                                        UserActionOperations.RESUME_MISSION -> "ادامه مأموریت"
                                        UserActionOperations.OPEN_GPX -> "دریافت فایل GPX"
                                        else -> operation
                                    },
                                    onClick = { onEvent(UserActionsUiEvent.RunOperation(item, operation)) },
                                    variant = FzButtonVariant.Secondary,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
                if (state.loading) item { FzLoading("در حال دریافت موارد بیشتر...") }
            }
        }
    }
}
