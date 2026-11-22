package Ir.co.tfs.farazaman.presentation.design.state

import Ir.co.tfs.farazaman.presentation.design.theme.FzSelectionMode
import org.junit.Assert.assertEquals
import org.junit.Test

class FzSelectionStateTest {
    @Test fun singleSelectionReplacesPreviousValue() {
        val state = FzSelectionState(FzSelectionMode.Single, listOf(1, 2), setOf(1))
        assertEquals(setOf(2), state.toggle(2).selectedValues)
    }

    @Test fun multipleSelectionCanToggleIndependentValues() {
        val state = FzSelectionState(FzSelectionMode.Multiple, listOf(1, 2), setOf(1))
        assertEquals(setOf(1, 2), state.toggle(2).selectedValues)
        assertEquals(emptySet<Int>(), state.toggle(1).selectedValues)
    }

    @Test fun disabledAndReadOnlyStateDoNotChange() {
        val disabled = FzSelectionState(FzSelectionMode.Toggle, listOf(1), enabled = false)
        val readOnly = FzSelectionState(FzSelectionMode.Toggle, listOf(1), readOnly = true)
        assertEquals(disabled, disabled.toggle(1))
        assertEquals(readOnly, readOnly.toggle(1))
    }
}
