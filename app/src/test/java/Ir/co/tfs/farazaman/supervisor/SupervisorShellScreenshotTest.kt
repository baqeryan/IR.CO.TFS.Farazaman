package Ir.co.tfs.farazaman.supervisor

import Ir.co.tfs.farazaman.presentation.compose.SupervisorShellScreen
import Ir.co.tfs.farazaman.presentation.compose.SupervisorShellTab
import Ir.co.tfs.farazaman.presentation.compose.SupervisorShellUiState
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import Ir.co.tfs.farazaman.presentation.design.theme.ThemeConfiguration
import Ir.co.tfs.farazaman.presentation.design.theme.ThemeMode
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.LayoutDirection
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SupervisorShellScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "fa-rIR-w360dp-h640dp")
    fun smallPhoneLightRtl() = capture(darkTheme = false, tab = SupervisorShellTab.Home)

    @Test
    @Config(qualifiers = "fa-rIR-w411dp-h891dp-night")
    fun normalPhoneDarkRtl() = capture(darkTheme = true, tab = SupervisorShellTab.Today)

    @Test
    @Config(qualifiers = "fa-rIR-w800dp-h1280dp")
    fun tabletLightRtl() = capture(darkTheme = false, tab = SupervisorShellTab.History)

    private fun capture(darkTheme: Boolean, tab: SupervisorShellTab) {
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                FaraZamanTheme(
                    ThemeConfiguration(mode = if (darkTheme) ThemeMode.Dark else ThemeMode.Light),
                ) {
                    SupervisorShellScreen(
                        state = SupervisorShellUiState(
                            selectedTab = tab,
                            missionTitle = "بازدید ناحیه مرکزی",
                            isTracking = tab == SupervisorShellTab.Today,
                        ),
                        onEvent = {},
                    )
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
