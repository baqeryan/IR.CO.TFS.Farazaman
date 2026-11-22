package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.presentation.design.preview.FzDarkPreview
import Ir.co.tfs.farazaman.presentation.design.preview.FzLightPreview
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import androidx.compose.runtime.Composable

private val supervisorPreviewItems = listOf(
    SupervisorTaskUiItem("1", "بازدید منطقه مرکزی", "خیابان نمونه"),
    SupervisorTaskUiItem("2", "کنترل وضعیت خدمات", "ناحیه دو"),
)

@FzLightPreview
@Composable
fun SupervisorHomeIdlePreview() {
    FaraZamanTheme {
        SupervisorHomeScreen(
            SupervisorHomeUiState(
                greeting = "سلام، ناظر فرازمان",
                date = "پنج‌شنبه، ۳ مهر",
                missionTitle = "شهرداری منطقه یک - قرارداد نظافت",
                dailyTasks = 4,
                continuousTasks = 2,
                violations = 1,
                unreadNotifications = 3,
            ),
            {},
        )
    }
}

@FzDarkPreview
@Composable
fun SupervisorHomeActiveDarkPreview() {
    FaraZamanTheme(darkTheme = true) {
        SupervisorHomeScreen(
            SupervisorHomeUiState(
                greeting = "سلام، ناظر فرازمان",
                date = "پنج‌شنبه، ۳ مهر",
                missionTitle = "شهرداری منطقه یک - قرارداد نظافت",
                tracking = true,
                insideWorkArea = false,
                dailyTasks = 4,
                continuousTasks = 2,
                violations = 1,
            ),
            {},
        )
    }
}

@FzLightPreview
@Composable
fun SupervisorShellLightPreview() {
    FaraZamanTheme {
        SupervisorShellScreen(
            SupervisorShellUiState(missionTitle = "بازدید ناحیه مرکزی"),
            {},
        )
    }
}

@FzDarkPreview
@Composable
fun SupervisorShellDarkPreview() {
    FaraZamanTheme(darkTheme = true) {
        SupervisorShellScreen(
            SupervisorShellUiState(isTracking = true, missionTitle = "بازدید فعال"),
            {},
        )
    }
}

@FzLightPreview
@Composable
fun SupervisorTodayContentPreview() {
    FaraZamanTheme {
        SupervisorTodayScreen(
            SupervisorTodayUiState(
                planningItems = supervisorPreviewItems,
                tracking = true,
                currentStreet = "خیابان ولیعصر، عرض بیش از ۲۵ متر",
            ),
            {},
        )
    }
}

@FzDarkPreview
@Composable
fun SupervisorTodayEmptyDarkPreview() {
    FaraZamanTheme(darkTheme = true) { SupervisorTodayScreen(SupervisorTodayUiState(), {}) }
}

@FzLightPreview
@Composable
fun SupervisorHistoryContentPreview() {
    FaraZamanTheme {
        SupervisorHistoryScreen(
            SupervisorHistoryUiState(
                dates = listOf(SupervisorHistoryDateUiItem("1405/7/3", "پنج‌شنبه", "۳", true)),
                items = listOf(
                    SupervisorHistoryEntryUiItem(
                        id = "1",
                        title = "نظافت معبر",
                        subtitle = "جمع‌آوری پسماند",
                        dateTimes = listOf("۱۴۰۵/۷/۳ - ۱۰:۳۰"),
                        badges = listOf(SupervisorHistoryBadgeUiItem("۱ ثبت شده", Ir.co.tfs.farazaman.presentation.design.components.FzStatusVariant.Warning)),
                    ),
                ),
            ),
            {},
        )
    }
}

@FzDarkPreview
@Composable
fun SupervisorHistoryLoadingDarkPreview() {
    FaraZamanTheme(darkTheme = true) {
        SupervisorHistoryScreen(SupervisorHistoryUiState(), {})
    }
}

@FzLightPreview
@Composable
fun ContractorViolationLocationPreview() {
    FaraZamanTheme {
        ContractorViolationLocationScreen(
            ContractorViolationLocationUiState(latitude = 35.7, longitude = 51.4, mapReady = true),
            {},
        )
    }
}
