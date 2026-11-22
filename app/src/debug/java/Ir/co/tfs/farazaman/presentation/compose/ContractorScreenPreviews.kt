package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.contractor.ContractorWorkContext
import Ir.co.tfs.farazaman.presentation.design.preview.FzDarkPreview
import Ir.co.tfs.farazaman.presentation.design.preview.FzLargeFontPreview
import Ir.co.tfs.farazaman.presentation.design.preview.FzLightPreview
import Ir.co.tfs.farazaman.presentation.design.preview.FzSmallPhonePreview
import androidx.compose.runtime.Composable

private val previewContext = ContractorWorkContext(
    organId = 1,
    organTitle = "منطقه نمونه",
    contractId = 1,
    contractTitle = "قرارداد نمونه",
)

@FzLightPreview
@FzSmallPhonePreview
@Composable
fun ContractorWorkAreaUnavailablePreview() {
    FaraZamanTheme {
        ContractorWorkAreaScreen(
            ContractorWorkAreaUiState("سرویس انتخاب محدوده هنوز متصل نشده است."),
            {},
        )
    }
}

@FzLightPreview
@Composable
fun ContractorOperationsLightPreview() {
    FaraZamanTheme {
        ContractorOperationsScreen(
            ContractorOperationsUiState(previewContext, "سرویس در حال اتصال است."),
            {},
        )
    }
}

@FzDarkPreview
@Composable
fun ContractorOperationsDarkActivePreview() {
    FaraZamanTheme(darkTheme = true) {
        ContractorOperationsScreen(
            ContractorOperationsUiState(previewContext, "عملیات فعال", operationActive = true),
            {},
        )
    }
}

@FzLightPreview
@FzLargeFontPreview
@Composable
fun ContractorActionValidationPreview() {
    FaraZamanTheme {
        ContractorActionScreen(
            ContractorActionUiState(
                description = "شرح اقدام",
                imageCount = 2,
                error = "ثبت حداقل یک تصویر الزامی است.",
                serviceMessage = "ارسال پس از اتصال سرویس فعال می‌شود.",
            ),
            {},
        )
    }
}

@FzLightPreview
@Composable
fun ContractorCorrectionsEmptyPreview() {
    FaraZamanTheme {
        ContractorCorrectionsScreen(
            ContractorCorrectionsUiState("اصلاحیه‌ای برای نمایش وجود ندارد."),
            {},
        )
    }
}

@FzDarkPreview
@Composable
fun ContractorCorrectionsLoadingPreview() {
    FaraZamanTheme(darkTheme = true) {
        ContractorCorrectionsScreen(
            ContractorCorrectionsUiState("لطفاً منتظر بمانید.", loading = true),
            {},
        )
    }
}

@FzLightPreview
@Composable
fun ContractorLocationReadyPreview() {
    FaraZamanTheme {
        ContractorViolationLocationScreen(
            ContractorViolationLocationUiState(latitude = 35.7, longitude = 51.4, mapReady = true),
            {},
        )
    }
}

@FzDarkPreview
@Composable
fun ContractorLocationErrorPreview() {
    FaraZamanTheme(darkTheme = true) {
        ContractorViolationLocationScreen(
            ContractorViolationLocationUiState(errorMessage = "نقشه در دسترس نیست."),
            {},
        )
    }
}
