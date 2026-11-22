package Ir.co.tfs.farazaman.support

import Ir.co.tfs.farazaman.data.model.AppNotificationDto
import Ir.co.tfs.farazaman.presentation.compose.NotificationsScreen
import Ir.co.tfs.farazaman.presentation.compose.NotificationsUiState
import Ir.co.tfs.farazaman.presentation.compose.SupervisorProfileScreen
import Ir.co.tfs.farazaman.presentation.compose.SupervisorProfileUiState
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import Ir.co.tfs.farazaman.presentation.design.theme.ThemeConfiguration
import Ir.co.tfs.farazaman.presentation.design.theme.ThemeMode
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "fa-rIR-w360dp-h800dp")
class SupportScreensScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val notification = AppNotificationDto(
        id = 125,
        title = "ماموریت جدید برای شما ثبت شد",
        createDate = "2026-09-10T10:00:00+03:30",
    )

    @Test
    fun notificationsContentLight() = capture {
        NotificationsScreen(NotificationsUiState(listOf(notification), hasMore = true), {})
    }

    @Test
    fun notificationsLoadingDark() = capture(dark = true) {
        NotificationsScreen(NotificationsUiState(loading = true), {})
    }

    @Test
    fun notificationsErrorLight() = capture {
        NotificationsScreen(NotificationsUiState(errorMessage = "ارتباط با سرویس برقرار نشد."), {})
    }

    @Test
    fun profileWithRoleChangeDark() = capture(dark = true) {
        SupervisorProfileScreen(
            SupervisorProfileUiState("مدیر سامانه", "admin", "مدیر", canChangeRole = true),
            {},
        )
    }

    private fun capture(
        dark: Boolean = false,
        content: @androidx.compose.runtime.Composable () -> Unit,
    ) {
        composeRule.setContent {
            FaraZamanTheme(
                ThemeConfiguration(mode = if (dark) ThemeMode.Dark else ThemeMode.Light),
                content = content,
            )
        }
        composeRule.onRoot().captureRoboImage()
    }
}
