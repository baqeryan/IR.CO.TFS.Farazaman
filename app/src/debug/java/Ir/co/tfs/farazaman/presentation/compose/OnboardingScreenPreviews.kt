package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.presentation.design.preview.FzDarkPreview
import Ir.co.tfs.farazaman.presentation.design.preview.FzLightPreview
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import androidx.compose.runtime.Composable

private val introPreviewPages = listOf(
    IntroPageUi(
        "شروع مأموریت",
        "مسیر حرکت شما هنگام مأموریت ثبت می‌شود.",
        R.drawable.intro_hands_phone,
        listOf("GPS را روشن کنید.", "مجوز موقعیت پس‌زمینه را فعال کنید."),
    ),
    IntroPageUi("ثبت نواقص", "نواقص مشاهده‌شده را همراه جزئیات ثبت کنید.", R.drawable.intro_map_pins),
)

@FzLightPreview
@Composable
fun IntroScreenPreview() {
    FaraZamanTheme {
        IntroScreen(IntroUiState("ناظر", introPreviewPages), onEvent = {})
    }
}

@FzDarkPreview
@Composable
fun DeveloperOptionsWarningPreview() {
    FaraZamanTheme(darkTheme = true) {
        DeveloperOptionsWarningScreen(
            state = DeveloperOptionsWarningUiState("برای ادامه، گزینه‌های توسعه‌دهنده را خاموش کنید."),
            onEvent = {},
        )
    }
}
