package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzButtonVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzCard
import Ir.co.tfs.farazaman.presentation.design.components.FzCardVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzHeaderSize
import Ir.co.tfs.farazaman.presentation.design.components.FzPageTemplate
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class ActionsNotificationsTab { Actions, Notifications }

sealed interface ActionsNotificationsUiEvent : UiEvent {
    data object Back : ActionsNotificationsUiEvent
    data class SelectTab(val tab: ActionsNotificationsTab) : ActionsNotificationsUiEvent
}

@Composable
fun ActionsNotificationsScreen(
    selectedTab: ActionsNotificationsTab,
    onEvent: (ActionsNotificationsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    val spacing = FaraZamanThemeTokens.spacing
    FzPageTemplate(
        title = "فعالیت‌ها و اعلان‌ها",
        headerSize = FzHeaderSize.Compact,
        onBack = { onEvent(ActionsNotificationsUiEvent.Back) },
        modifier = modifier,
    ) { _ ->
        Column(
            Modifier.fillMaxSize().padding(
                start = spacing.sm,
                top = spacing.sm,
                end = spacing.sm,
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            FzCard(
                modifier = Modifier.fillMaxWidth(),
                variant = FzCardVariant.Glass,
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(spacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                ) {
                    FzButton(
                    text = "فعالیت‌ها",
                        onClick = { onEvent(ActionsNotificationsUiEvent.SelectTab(ActionsNotificationsTab.Actions)) },
                        modifier = Modifier.weight(1f),
                        variant = if (selectedTab == ActionsNotificationsTab.Actions) FzButtonVariant.Primary else FzButtonVariant.Ghost,
                    )
                    FzButton(
                    text = "اعلان‌ها",
                        onClick = { onEvent(ActionsNotificationsUiEvent.SelectTab(ActionsNotificationsTab.Notifications)) },
                        modifier = Modifier.weight(1f),
                        variant = if (selectedTab == ActionsNotificationsTab.Notifications) FzButtonVariant.Primary else FzButtonVariant.Ghost,
                    )
                }
            }
            content(Modifier.fillMaxSize())
        }
    }
}
