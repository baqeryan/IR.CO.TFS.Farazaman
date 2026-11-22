package Ir.co.tfs.farazaman.system

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import Ir.co.tfs.farazaman.map.FakeComposeMapHost
import Ir.co.tfs.farazaman.presentation.compose.*
import Ir.co.tfs.farazaman.presentation.compose.track.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SystemFlowsScreenshotTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun driverFlow() {
        composeRule.setContent { FaraZamanTheme { DriverScreen(DriverUiState(hasActiveMission = true, zoneSummary = "منطقه مرکزی", trackpointsCount = 12), {}) } }
        composeRule.onRoot().captureRoboImage()
    }

    @Test fun trackFlow() {
        composeRule.setContent { FaraZamanTheme { TrackListScreen(TrackListUiState(tracks = listOf(TrackListItemUiState(1, "مسیر امروز", "امروز", 12))), {}) } }
        composeRule.onRoot().captureRoboImage()
    }

    @Test fun mapFlow() {
        composeRule.setContent { FaraZamanTheme { FakeComposeMapHost() } }
        composeRule.onRoot().captureRoboImage()
    }

    @Test fun violationFlow() {
        composeRule.setContent { FaraZamanTheme { ViolationFormScreen(ViolationFormUiState(description = "نمونه"), {}) } }
        composeRule.onRoot().captureRoboImage()
    }
}
