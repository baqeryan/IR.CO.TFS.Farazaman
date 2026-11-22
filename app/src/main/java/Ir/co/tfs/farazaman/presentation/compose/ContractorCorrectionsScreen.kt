package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzButtonVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzEmptyState
import Ir.co.tfs.farazaman.presentation.design.components.FzHeaderSize
import Ir.co.tfs.farazaman.presentation.design.components.FzPageTemplate
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class ContractorCorrectionsUiState(
    val serviceMessage: String,
    val loading: Boolean = false,
)

sealed interface ContractorCorrectionsEvent {
    data object Back : ContractorCorrectionsEvent
    data class OpenLocation(val latitude: Double, val longitude: Double) : ContractorCorrectionsEvent
}

object ContractorCorrectionsReducer {
    fun loadingChanged(state: ContractorCorrectionsUiState, loading: Boolean) = state.copy(loading = loading)
}

@Composable
fun ContractorCorrectionsScreen(
    state: ContractorCorrectionsUiState,
    onEvent: (ContractorCorrectionsEvent) -> Unit,
    modifier: Modifier = Modifier,
) = FzPageTemplate(title = "اصلاحیه نواقص", headerSize = FzHeaderSize.Compact, modifier = modifier, onBack = { onEvent(ContractorCorrectionsEvent.Back) }) { padding ->
    Column(Modifier.fillMaxSize().padding(padding)) {
        FzEmptyState(
            if (state.loading) "در حال دریافت اصلاحیه‌ها" else "موردی ثبت نشده",
            state.serviceMessage,
        )
    }
}
