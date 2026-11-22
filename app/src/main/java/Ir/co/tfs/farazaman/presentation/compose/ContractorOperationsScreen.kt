package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.contractor.ContractorWorkContext
import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzButtonVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzCard
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

data class ContractorOperationsUiState(
    val context: ContractorWorkContext,
    val serviceMessage: String,
    val operationActive: Boolean = false,
)

sealed interface ContractorOperationsEvent {
    data object Back : ContractorOperationsEvent
    data object StartOperation : ContractorOperationsEvent
    data object RegisterAction : ContractorOperationsEvent
    data object Corrections : ContractorOperationsEvent
    data object ChangeWorkArea : ContractorOperationsEvent
}

object ContractorOperationsReducer {
    fun operationStatusChanged(state: ContractorOperationsUiState, active: Boolean) =
        state.copy(operationActive = active)
}

@Composable
fun ContractorOperationsScreen(
    state: ContractorOperationsUiState,
    onEvent: (ContractorOperationsEvent) -> Unit,
    modifier: Modifier = Modifier,
) = FzPageTemplate(title = "عملیات پیمانکار", modifier = modifier, onBack = { onEvent(ContractorOperationsEvent.Back) }) { padding ->
    val spacing = FaraZamanThemeTokens.spacing
    Column(
        Modifier.fillMaxSize().padding(padding),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        FzCard {
            Column(Modifier.padding(spacing.md)) {
                Text(state.context.organTitle)
                Text(state.context.contractTitle)
            }
        }
        FzButton(
            if (state.operationActive) "عملیات فعال است" else "شروع عملیات",
            { onEvent(ContractorOperationsEvent.StartOperation) },
            modifier = Modifier.fillMaxWidth(),
        )
        FzButton("ثبت اقدام", { onEvent(ContractorOperationsEvent.RegisterAction) }, modifier = Modifier.fillMaxWidth(), variant = FzButtonVariant.Secondary)
        FzButton("اصلاحیه نواقص", { onEvent(ContractorOperationsEvent.Corrections) }, modifier = Modifier.fillMaxWidth(), variant = FzButtonVariant.Secondary)
        FzButton("تغییر محدوده کاری", { onEvent(ContractorOperationsEvent.ChangeWorkArea) }, modifier = Modifier.fillMaxWidth(), variant = FzButtonVariant.Ghost)
        Text(state.serviceMessage, style = MaterialTheme.typography.bodySmall)
    }
}
