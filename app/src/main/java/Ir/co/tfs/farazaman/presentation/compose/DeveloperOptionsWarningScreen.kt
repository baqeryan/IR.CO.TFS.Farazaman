package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzButtonVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzCard
import Ir.co.tfs.farazaman.presentation.design.components.FzHeaderSize
import Ir.co.tfs.farazaman.presentation.design.components.FzPageTemplate
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

data class DeveloperOptionsWarningUiState(val message: String)

sealed interface DeveloperOptionsWarningEvent : UiEvent {
    data object OpenSettings : DeveloperOptionsWarningEvent
    data object Recheck : DeveloperOptionsWarningEvent
}

@Composable
fun DeveloperOptionsWarningScreen(
    state: DeveloperOptionsWarningUiState,
    onEvent: (DeveloperOptionsWarningEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FaraZamanThemeTokens.spacing
    FzPageTemplate(title = "هشدار امنیتی", headerSize = FzHeaderSize.Compact, modifier = modifier) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
        ) {
            FzCard(Modifier.fillMaxWidth()) {
                Text(
                    state.message,
                    modifier = Modifier.padding(spacing.lg),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            FzButton("باز کردن تنظیمات", { onEvent(DeveloperOptionsWarningEvent.OpenSettings) }, Modifier.fillMaxWidth())
            FzButton("بررسی مجدد", { onEvent(DeveloperOptionsWarningEvent.Recheck) }, Modifier.fillMaxWidth(), FzButtonVariant.Secondary)
        }
    }
}
