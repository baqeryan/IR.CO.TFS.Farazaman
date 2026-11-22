package Ir.co.tfs.farazaman.contractor

import Ir.co.tfs.farazaman.presentation.compose.ContractorActionEvent
import Ir.co.tfs.farazaman.presentation.compose.ContractorActionScreen
import Ir.co.tfs.farazaman.presentation.compose.ContractorActionUiState
import Ir.co.tfs.farazaman.presentation.compose.ContractorOperationsEvent
import Ir.co.tfs.farazaman.presentation.compose.ContractorOperationsScreen
import Ir.co.tfs.farazaman.presentation.compose.ContractorOperationsUiState
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ContractorFlowTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun operationsExposeContractorActionsAndEmitNavigationEvent() {
        var correctionOpened = false
        composeRule.setContent {
            FaraZamanTheme {
                ContractorOperationsScreen(
                    state = ContractorOperationsUiState(
                        context = ContractorWorkContext(1, "ناحیه مرکزی", 2, "قرارداد خدمات"),
                        serviceMessage = "",
                    ),
                    onEvent = { correctionOpened = it == ContractorOperationsEvent.Corrections },
                )
            }
        }

        composeRule.onNodeWithText("شروع عملیات").assertIsDisplayed()
        composeRule.onNodeWithText("ثبت اقدام").assertIsDisplayed()
        composeRule.onNodeWithText("اصلاحیه تخلفات").performClick()
        composeRule.runOnIdle { assertTrue(correctionOpened) }
    }

    @Test
    fun actionFormEmitsDescriptionAndCameraEvents() {
        val events = mutableListOf<ContractorActionEvent>()
        composeRule.setContent {
            FaraZamanTheme {
                ContractorActionScreen(
                    state = ContractorActionUiState(serviceMessage = ""),
                    onEvent = events::add,
                )
            }
        }

        composeRule.onNodeWithText("توضیحات اقدام").performTextInput("اقدام انجام شد")
        composeRule.onNodeWithText("افزودن تصویر").performClick()
        composeRule.runOnIdle {
            assertEquals(ContractorActionEvent.DescriptionChanged("اقدام انجام شد"), events.first())
            assertEquals(ContractorActionEvent.CaptureImage, events.last())
        }
    }
}
