package Ir.co.tfs.farazaman.presentation.design

import Ir.co.tfs.farazaman.presentation.design.components.FzChart
import Ir.co.tfs.farazaman.presentation.design.components.FzChartPoint
import Ir.co.tfs.farazaman.presentation.design.components.FzChartType
import Ir.co.tfs.farazaman.presentation.design.components.FzVirtualizedList
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FzDataComponentsTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun virtualizedPersianRowsAndChartRender() {
        composeRule.setContent {
            FaraZamanTheme {
                Column {
                    FzVirtualizedList((1..100).toList(), { it }, Modifier.height(120.dp)) { Text("ردیف $it") }
                    FzChart(listOf(FzChartPoint("امروز", 8f), FzChartPoint("دیروز", 5f)), FzChartType.Line)
                }
            }
        }
        composeRule.onNodeWithText("ردیف 1").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage()
    }
}
