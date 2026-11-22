package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzChoice
import Ir.co.tfs.farazaman.presentation.design.components.FzHeaderSize
import Ir.co.tfs.farazaman.presentation.design.components.FzPageTemplate
import Ir.co.tfs.farazaman.presentation.design.components.FzSelect
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class SupervisorWorkAreaUiState(
    val selectedContractTypeTitle: String? = null,
    val selectedTenantId: Int? = null,
    val selectedOrganTitle: String? = null,
    val selectedContractTitle: String? = null,
    val selectedOrganId: Int? = null,
    val selectedContractId: Int? = null,
    val organOptions: List<SupervisorWorkAreaOption> = emptyList(),
    val contractOptions: List<SupervisorWorkAreaOption> = emptyList(),
    val contractTypeOptions: List<SupervisorWorkAreaOption> = emptyList(),
    val loadingContractTypes: Boolean = false,
    val loadingOrgans: Boolean = false,
    val loadingContracts: Boolean = false,
    val submitting: Boolean = false,
    val errorMessage: String? = null,
) {
    val canSubmit: Boolean get() = selectedTenantId != null && selectedOrganTitle != null && selectedContractTitle != null && !submitting
}

data class SupervisorWorkAreaOption(val id: Int, val title: String)

sealed interface SupervisorWorkAreaEvent : UiEvent {
    data object OpenProfile : SupervisorWorkAreaEvent
    data class SelectContractType(val tenantId: Int) : SupervisorWorkAreaEvent
    data class SelectOrgan(val id: Int) : SupervisorWorkAreaEvent
    data class SelectContract(val id: Int) : SupervisorWorkAreaEvent
    data object Submit : SupervisorWorkAreaEvent
}

@Composable
fun SupervisorWorkAreaScreen(
    state: SupervisorWorkAreaUiState,
    onEvent: (SupervisorWorkAreaEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FaraZamanThemeTokens.spacing
    FzPageTemplate(
        title = "انتخاب محدوده کاری",
        subtitle = "برای دریافت برنامه کاری، کارفرما و قرارداد را انتخاب کنید.",
        headerSize = FzHeaderSize.Expanded,
        headerEnd = { SupervisorProfileAction(onClick = { onEvent(SupervisorWorkAreaEvent.OpenProfile) }) },
        modifier = modifier,
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            FzSelect(
                value = state.selectedTenantId,
                options = state.contractTypeOptions.map { FzChoice(it.id, it.title) },
                onValueChange = { onEvent(SupervisorWorkAreaEvent.SelectContractType(it)) },
                label = "نوع قرارداد",
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.loadingContractTypes,
                loading = state.loadingContractTypes,
            )
            FzSelect(
                value = state.selectedOrganId,
                options = state.organOptions.map { FzChoice(it.id, it.title) },
                onValueChange = { onEvent(SupervisorWorkAreaEvent.SelectOrgan(it)) },
                label = "کارفرما",
                enabled = state.selectedTenantId != null && !state.loadingOrgans,
                loading = state.loadingOrgans,
                modifier = Modifier.fillMaxWidth(),
            )
            FzSelect(
                value = state.selectedContractId,
                options = state.contractOptions.map { FzChoice(it.id, it.title) },
                onValueChange = { onEvent(SupervisorWorkAreaEvent.SelectContract(it)) },
                label = "قرارداد",
                enabled = state.selectedOrganTitle != null && !state.loadingContracts,
                loading = state.loadingContracts,
                modifier = Modifier.fillMaxWidth(),
            )
            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            FzButton(
                if (state.submitting) "در حال ثبت..." else "تأیید و ادامه",
                { onEvent(SupervisorWorkAreaEvent.Submit) },
                enabled = state.canSubmit,
                loading = state.submitting,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "انتخاب محدوده کاری",
    showBackground = true,
    locale = "fa",
    widthDp = 393,
    heightDp = 852,
)
@Composable
private fun SupervisorWorkAreaScreenPreview() {
    FaraZamanTheme {
        SupervisorWorkAreaScreen(
            state = SupervisorWorkAreaUiState(
                selectedContractTypeTitle = "خدمات شهری",
                selectedTenantId = 1,
                selectedOrganTitle = "منطقه ۱ ناحیه ۱",
                selectedContractTitle = "۱۳/۶۱۰۰ - خدمات شهری",
                selectedOrganId = 8002,
                selectedContractId = 1,
                contractTypeOptions = listOf(SupervisorWorkAreaOption(1, "خدمات شهری")),
                organOptions = listOf(
                    SupervisorWorkAreaOption(8002, "منطقه ۱ ناحیه ۱"),
                    SupervisorWorkAreaOption(8003, "منطقه ۱ ناحیه ۲"),
                ),
                contractOptions = listOf(
                    SupervisorWorkAreaOption(1, "۱۳/۶۱۰۰ - خدمات شهری"),
                    SupervisorWorkAreaOption(2, "جمع‌آوری و پاک‌سازی معابر"),
                ),
            ),
            onEvent = {},
        )
    }
}
