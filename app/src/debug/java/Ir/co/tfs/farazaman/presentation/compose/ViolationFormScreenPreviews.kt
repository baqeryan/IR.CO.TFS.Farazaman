package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.presentation.design.preview.FzDarkPreview
import Ir.co.tfs.farazaman.presentation.design.preview.FzLargeFontPreview
import Ir.co.tfs.farazaman.presentation.design.preview.FzLightPreview
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier

private val violationPreviewState = ViolationFormUiState(
    selectors = listOf(
        ViolationSelectorUiState("کارفرما", "شهرداری منطقه مرکزی"),
        ViolationSelectorUiState("قرارداد", "قرارداد خدمات شهری"),
        ViolationSelectorUiState("گروه نقص", "نظافت معابر"),
        ViolationSelectorUiState("عنوان نقص", "انباشت پسماند"),
    ),
    count = "2",
    address = "خیابان نمونه، کوچه یکم",
    description = "نیازمند رسیدگی",
    imageCount = 2,
)

@FzLightPreview
@Composable
fun ViolationFormContentPreview() {
    FaraZamanTheme { ViolationFormScreen(violationPreviewState, {}) }
}

@FzDarkPreview
@Composable
fun ViolationFormSubmittingDarkPreview() {
    FaraZamanTheme(darkTheme = true) {
        ViolationFormScreen(violationPreviewState.copy(submitting = true), {})
    }
}

@FzLargeFontPreview
@Composable
fun ViolationFormValidationLargeFontPreview() {
    FaraZamanTheme {
        ViolationFormScreen(
            violationPreviewState.copy(description = "", fieldError = "توضیحات الزامی است."),
            {},
        )
    }
}

@FzLightPreview
@Composable
fun ViolationDetailsSheetPreview() {
    FaraZamanTheme {
        ViolationDetailsSheet(
            title = "جزئیات نقص نظافت معبر",
            subtitle = "خیابان نمونه",
            rows = listOf(
                ViolationDetailUiItem("تاریخ", "۱۴۰۵/۰۷/۰۳"),
                ViolationDetailUiItem("کارفرما", "شهرداری منطقه یک"),
                ViolationDetailUiItem("قرارداد", "خدمات شهری"),
            ),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@FzDarkPreview
@Composable
fun ViolationImagesSheetDarkPreview() {
    FaraZamanTheme(darkTheme = true) {
        ViolationImagesSheet(emptyList(), {}, {}, Modifier.fillMaxSize())
    }
}
