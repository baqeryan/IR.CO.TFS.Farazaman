package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzEmptyState
import Ir.co.tfs.farazaman.presentation.design.components.FzLoading
import Ir.co.tfs.farazaman.presentation.design.components.FzHeaderSize
import Ir.co.tfs.farazaman.presentation.design.components.FzPageTemplate
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class ContractorViolationLocationUiState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val mapReady: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface ContractorViolationLocationEvent {
    data object Back : ContractorViolationLocationEvent
    data object Retry : ContractorViolationLocationEvent
}

sealed interface ContractorViolationLocationMutation {
    data object Loading : ContractorViolationLocationMutation
    data class Ready(val latitude: Double, val longitude: Double) : ContractorViolationLocationMutation
    data class Failed(val message: String) : ContractorViolationLocationMutation
}

object ContractorViolationLocationReducer {
    fun reduce(state: ContractorViolationLocationUiState, mutation: ContractorViolationLocationMutation) =
        when (mutation) {
            ContractorViolationLocationMutation.Loading -> state.copy(mapReady = false, errorMessage = null)
            is ContractorViolationLocationMutation.Ready -> state.copy(
                latitude = mutation.latitude,
                longitude = mutation.longitude,
                mapReady = true,
                errorMessage = null,
            )
            is ContractorViolationLocationMutation.Failed -> state.copy(
                mapReady = false,
                errorMessage = mutation.message,
            )
        }
}

@Composable
fun ContractorViolationLocationScreen(
    state: ContractorViolationLocationUiState,
    onEvent: (ContractorViolationLocationEvent) -> Unit,
    modifier: Modifier = Modifier,
    mapContent: @Composable () -> Unit = {
        Text("${state.latitude}, ${state.longitude}")
    },
) {
    FzPageTemplate(title = "موقعیت نقص", headerSize = FzHeaderSize.Compact, modifier = modifier, onBack = { onEvent(ContractorViolationLocationEvent.Back) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (state.errorMessage != null) {
                FzEmptyState("موقعیت در دسترس نیست", state.errorMessage)
                FzButton("تلاش مجدد", { onEvent(ContractorViolationLocationEvent.Retry) })
            } else {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    mapContent()
                    if (!state.mapReady) FzLoading("در حال آماده‌سازی نقشه")
                }
            }
        }
    }
}
