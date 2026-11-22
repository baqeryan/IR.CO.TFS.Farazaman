package Ir.co.tfs.farazaman.supervisor

import Ir.co.tfs.farazaman.presentation.compose.SupervisorShellEvent
import Ir.co.tfs.farazaman.presentation.compose.SupervisorShellScreen
import Ir.co.tfs.farazaman.presentation.compose.SupervisorShellTab
import Ir.co.tfs.farazaman.presentation.compose.SupervisorShellUiState
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import Ir.co.tfs.farazaman.presentation.design.theme.ThemeConfiguration
import Ir.co.tfs.farazaman.presentation.design.theme.ThemeMode
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SupervisorShellTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rtlGlassNavigationEmitsSelectedTabAndKeepsContentVisible() {
        var selected: SupervisorShellTab? = null
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                FaraZamanTheme {
                    SupervisorShellScreen(
                        state = SupervisorShellUiState(missionTitle = "ماموریت ناحیه مرکزی"),
                        onEvent = { event ->
                            if (event is SupervisorShellEvent.SelectTab) selected = event.tab
                        },
                    )
                }
            }
        }

        composeRule.onNodeWithText("ماموریت ناحیه مرکزی").assertIsDisplayed()
        composeRule.onNodeWithText("تاریخچه").assertIsDisplayed().performClick()
        composeRule.runOnIdle { assertEquals(SupervisorShellTab.History, selected) }
    }

    @Test
    fun darkThemeKeepsNavigationAndActiveVisitReadable() {
        composeRule.setContent {
            FaraZamanTheme(ThemeConfiguration(mode = ThemeMode.Dark)) {
                SupervisorShellScreen(
                    state = SupervisorShellUiState(isTracking = true, missionTitle = "بازدید فعال"),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithText("بازدید فعال است").assertIsDisplayed()
        composeRule.onNodeWithText("خانه").assertIsDisplayed()
        composeRule.onNodeWithText("امروز").assertIsDisplayed()
        composeRule.onNodeWithText("تاریخچه").assertIsDisplayed()
    }
}
