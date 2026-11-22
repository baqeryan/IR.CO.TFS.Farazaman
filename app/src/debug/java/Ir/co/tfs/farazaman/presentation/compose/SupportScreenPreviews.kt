package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.data.model.AppNotificationDto
import Ir.co.tfs.farazaman.presentation.design.preview.FzDarkPreview
import Ir.co.tfs.farazaman.presentation.design.preview.FzLightPreview
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import androidx.compose.runtime.Composable

private val previewNotification = AppNotificationDto(
    id = 125,
    title = "ماموریت جدید برای شما ثبت شد",
    createDate = "2026-09-06T14:30:00+03:30",
)

@FzLightPreview
@Composable
fun NotificationsContentPreview() {
    FaraZamanTheme {
        NotificationsScreen(NotificationsUiState(items = listOf(previewNotification)), {})
    }
}

@FzDarkPreview
@Composable
fun NotificationsEmptyPreview() {
    FaraZamanTheme(darkTheme = true) {
        NotificationsScreen(NotificationsUiState(), {})
    }
}

@FzLightPreview
@Composable
fun NotificationsErrorPreview() {
    FaraZamanTheme {
        NotificationsScreen(NotificationsUiState(errorMessage = "ارتباط با سرویس اعلان برقرار نشد."), {})
    }
}

@FzLightPreview
@Composable
fun ActionsNotificationsShellPreview() {
    FaraZamanTheme {
        ActionsNotificationsScreen(
            selectedTab = ActionsNotificationsTab.Notifications,
            onEvent = {},
        ) { modifier ->
            NotificationsScreen(NotificationsUiState(), {}, modifier)
        }
    }
}

@FzLightPreview
@Composable
fun PreferencesMatrixPreview() {
    FaraZamanTheme { PreferencesScreen(PreferencesUiState(), {}) }
}

@FzLightPreview
@Composable
fun AboutMatrixPreview() {
    FaraZamanTheme { AboutScreen(AboutUiState("1.0", "درباره برنامه"), {}) }
}

@FzLightPreview
@Composable
fun SupervisorProfileMatrixPreview() {
    FaraZamanTheme {
        SupervisorProfileScreen(
            SupervisorProfileUiState("کاربر فرازمان", "supervisor", "ناظر قرارداد", true),
            {},
        )
    }
}

@FzLightPreview
@Composable
fun SupervisorWorkAreaMatrixPreview() {
    FaraZamanTheme {
        SupervisorWorkAreaScreen(
            SupervisorWorkAreaUiState(
                selectedContractTypeTitle = "خدمات شهری",
                selectedTenantId = 1,
                selectedOrganTitle = "شهرداری نمونه",
                selectedContractTitle = "قرارداد خدمات شهری",
            ),
            {},
        )
    }
}

@FzLightPreview
@Composable
fun UserActionsEmptyMatrixPreview() {
    FaraZamanTheme { UserActionsScreen(UserActionsUiState(), {}) }
}

@FzLightPreview
@Composable
fun ZoneSelectionEmptyMatrixPreview() {
    FaraZamanTheme { ZoneSelectionScreen(ZoneSelectionUiState(), {}) }
}
