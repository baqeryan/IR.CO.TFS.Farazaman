package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FzComponentSize
import Ir.co.tfs.farazaman.presentation.design.theme.FzComponentSizes
import org.junit.Assert.assertTrue
import org.junit.Test

class FzButtonContractTest {
    @Test fun allButtonSizesMeetMinimumTouchTarget() {
        val sizes = FzComponentSizes()
        FzComponentSize.values().forEach { size ->
            assertTrue(sizes.buttonHeight(size) >= sizes.minimumTouchTarget)
        }
    }

    @Test fun buttonSizesAreOrdered() {
        val sizes = FzComponentSizes()
        assertTrue(sizes.buttonHeight(FzComponentSize.Small) <= sizes.buttonHeight(FzComponentSize.Medium))
        assertTrue(sizes.buttonHeight(FzComponentSize.Medium) <= sizes.buttonHeight(FzComponentSize.Large))
    }
}
