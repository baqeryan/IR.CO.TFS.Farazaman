package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SupervisorWorkAreaScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingKeepsAllSelectFieldsVisible() {
        composeRule.setContent {
            FaraZamanTheme {
                SupervisorWorkAreaScreen(
                    state = SupervisorWorkAreaUiState(
                        loadingContractTypes = true,
                        loadingOrgans = true,
                        loadingContracts = true,
                    ),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithText("نوع قرارداد").assertIsDisplayed()
        composeRule.onNodeWithText("کارفرما").assertIsDisplayed()
        composeRule.onNodeWithText("قرارداد").assertIsDisplayed()
    }
}
