package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzButtonVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzHeaderSize
import Ir.co.tfs.farazaman.presentation.design.components.FzPageTemplate
import Ir.co.tfs.farazaman.presentation.design.components.FzTextField
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

data class ContractorActionUiState(
    val description: String = "",
    val imageCount: Int = 0,
    val error: String? = null,
    val serviceMessage: String,
)

sealed interface ContractorActionEvent {
    data class DescriptionChanged(val value: String) : ContractorActionEvent
    data object CaptureImage : ContractorActionEvent
    data object Submit : ContractorActionEvent
    data object Back : ContractorActionEvent
}

sealed interface ContractorActionMutation {
    data class DescriptionChanged(val value: String) : ContractorActionMutation
    data class ImageCountChanged(val count: Int) : ContractorActionMutation
    data class ValidationFailed(val message: String) : ContractorActionMutation
}

object ContractorActionReducer {
    fun reduce(state: ContractorActionUiState, mutation: ContractorActionMutation) = when (mutation) {
        is ContractorActionMutation.DescriptionChanged -> state.copy(description = mutation.value, error = null)
        is ContractorActionMutation.ImageCountChanged -> state.copy(imageCount = mutation.count.coerceAtLeast(0))
        is ContractorActionMutation.ValidationFailed -> state.copy(error = mutation.message)
    }
}

@Composable
fun ContractorActionScreen(
    state: ContractorActionUiState,
    onEvent: (ContractorActionEvent) -> Unit,
    modifier: Modifier = Modifier,
) = FzPageTemplate(title = "ثبت اقدام", headerSize = FzHeaderSize.Compact, modifier = modifier, onBack = { onEvent(ContractorActionEvent.Back) }) { padding ->
    val spacing = FaraZamanThemeTokens.spacing
    Column(
        Modifier.fillMaxSize().padding(padding),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        FzTextField(state.description, { onEvent(ContractorActionEvent.DescriptionChanged(it)) }, "توضیحات اقدام", errorText = state.error)
        Text("تصاویر: ${state.imageCount}")
        FzButton("افزودن تصویر", { onEvent(ContractorActionEvent.CaptureImage) }, modifier = Modifier.fillMaxWidth(), variant = FzButtonVariant.Secondary)
        FzButton("ثبت", { onEvent(ContractorActionEvent.Submit) }, modifier = Modifier.fillMaxWidth())
        Text(state.serviceMessage, style = MaterialTheme.typography.bodySmall)
    }
}
