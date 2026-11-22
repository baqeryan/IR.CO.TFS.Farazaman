package Ir.co.tfs.farazaman.contractor

import Ir.co.tfs.farazaman.presentation.compose.ContractorActionScreen
import Ir.co.tfs.farazaman.presentation.compose.ContractorActionUiState
import Ir.co.tfs.farazaman.presentation.compose.ContractorCorrectionsScreen
import Ir.co.tfs.farazaman.presentation.compose.ContractorCorrectionsUiState
import Ir.co.tfs.farazaman.presentation.compose.ContractorOperationsScreen
import Ir.co.tfs.farazaman.presentation.compose.ContractorOperationsUiState
import Ir.co.tfs.farazaman.presentation.compose.ContractorViolationLocationScreen
import Ir.co.tfs.farazaman.presentation.compose.ContractorViolationLocationUiState
import Ir.co.tfs.farazaman.presentation.compose.ContractorWorkAreaScreen
import Ir.co.tfs.farazaman.presentation.compose.ContractorWorkAreaUiState
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import Ir.co.tfs.farazaman.presentation.design.theme.ThemeConfiguration
import Ir.co.tfs.farazaman.presentation.design.theme.ThemeMode
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
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
class ContractorScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context = ContractorWorkContext(1, "ناحیه مرکزی", 2, "قرارداد خدمات")

    @Test
    fun workAreaUnavailable() = capture {
        ContractorWorkAreaScreen(ContractorWorkAreaUiState("سرویس هنوز متصل نشده است."), {})
    }

    @Test
    fun operationsContent() = capture {
        ContractorOperationsScreen(ContractorOperationsUiState(context, "آماده شروع عملیات"), {})
    }

    @Test
    fun actionValidationLargeFont() = capture(fontScale = 1.5f) {
        ContractorActionScreen(
            ContractorActionUiState(error = "توضیحات الزامی است", serviceMessage = "", imageCount = 1),
            {},
        )
    }

    @Test
    fun correctionsEmptyDark() = capture(dark = true) {
        ContractorCorrectionsScreen(ContractorCorrectionsUiState("اصلاحیه‌ای وجود ندارد."), {})
    }

    @Test
    fun locationWithDeterministicMapFake() = capture {
        ContractorViolationLocationScreen(
            ContractorViolationLocationUiState(35.7, 51.4, mapReady = true),
            {},
        ) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
        }
    }

    private fun capture(
        dark: Boolean = false,
        fontScale: Float = 1f,
        content: @androidx.compose.runtime.Composable () -> Unit,
    ) {
        composeRule.setContent {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(1f, fontScale),
            ) {
                FaraZamanTheme(
                    ThemeConfiguration(mode = if (dark) ThemeMode.Dark else ThemeMode.Light),
                    content = content,
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
