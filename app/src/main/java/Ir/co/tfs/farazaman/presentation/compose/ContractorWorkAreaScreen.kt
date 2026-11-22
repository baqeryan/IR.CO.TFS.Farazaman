package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzButtonVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzEmptyState
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

data class ContractorWorkAreaUiState(
    val serviceMessage: String,
    val hasContext: Boolean = false,
)

sealed interface ContractorWorkAreaEvent {
    data object Back : ContractorWorkAreaEvent
    data object Profile : ContractorWorkAreaEvent
    data object Continue : ContractorWorkAreaEvent
}

sealed interface ContractorWorkAreaMutation {
    data class ContextAvailabilityChanged(val available: Boolean) : ContractorWorkAreaMutation
    data class ServiceMessageChanged(val message: String) : ContractorWorkAreaMutation
}

object ContractorWorkAreaReducer {
    fun reduce(state: ContractorWorkAreaUiState, mutation: ContractorWorkAreaMutation) = when (mutation) {
        is ContractorWorkAreaMutation.ContextAvailabilityChanged -> state.copy(hasContext = mutation.available)
        is ContractorWorkAreaMutation.ServiceMessageChanged -> state.copy(serviceMessage = mutation.message)
    }
}

@Composable
fun ContractorWorkAreaScreen(
    state: ContractorWorkAreaUiState,
    onEvent: (ContractorWorkAreaEvent) -> Unit,
    modifier: Modifier = Modifier,
) = FzPageTemplate(title = "انتخاب محدوده کاری", subtitle = "محدوده فعالیت پیمانکار را انتخاب کنید", headerSize = FzHeaderSize.Expanded, modifier = modifier, onBack = { onEvent(ContractorWorkAreaEvent.Back) }) { padding ->
    val spacing = FaraZamanThemeTokens.spacing
    Column(
        Modifier.fillMaxSize().padding(padding),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        FzEmptyState("انتخاب محدوده کاری", state.serviceMessage)
        if (state.hasContext) {
            FzButton("ادامه", { onEvent(ContractorWorkAreaEvent.Continue) }, modifier = Modifier.fillMaxWidth())
        }
    }
}
