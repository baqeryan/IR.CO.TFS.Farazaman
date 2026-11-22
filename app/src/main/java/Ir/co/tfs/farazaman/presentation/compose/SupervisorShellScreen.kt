package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzCard
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class SupervisorShellTab { Home, Today, History }

data class SupervisorShellUiState(
    val selectedTab: SupervisorShellTab = SupervisorShellTab.Home,
    val title: String = "ناظر",
    val missionTitle: String? = null,
    val isTracking: Boolean = false,
)

sealed interface SupervisorShellEvent {
    data class SelectTab(val tab: SupervisorShellTab) : SupervisorShellEvent
    data object StartVisit : SupervisorShellEvent
    data object OpenNotifications : SupervisorShellEvent
}

object SupervisorShellReducer {
    fun reduce(
        state: SupervisorShellUiState,
        event: SupervisorShellEvent,
    ): SupervisorShellUiState = when (event) {
        is SupervisorShellEvent.SelectTab -> state.copy(selectedTab = event.tab)
        SupervisorShellEvent.StartVisit,
        SupervisorShellEvent.OpenNotifications,
        -> state
    }
}

@Composable
fun SupervisorShellScreen(
    state: SupervisorShellUiState,
    onEvent: (SupervisorShellEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FaraZamanThemeTokens.spacing
    SupervisorScaffold(
        selectedTab = state.selectedTab,
        onTabSelected = { onEvent(SupervisorShellEvent.SelectTab(it)) },
        modifier = modifier,
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(state.title, style = MaterialTheme.typography.headlineSmall)
                SupervisorNotificationAction(
                    unreadCount = 0,
                    onClick = { onEvent(SupervisorShellEvent.OpenNotifications) },
                )
            }
            FzCard {
                Column(Modifier.padding(spacing.md), verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    Text(state.missionTitle ?: "ماموریت فعالی انتخاب نشده است")
                    FzButton(
                        if (state.isTracking) "بازدید فعال است" else "شروع بازدید",
                        { onEvent(SupervisorShellEvent.StartVisit) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
