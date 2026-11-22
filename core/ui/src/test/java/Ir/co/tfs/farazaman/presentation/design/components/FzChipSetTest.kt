package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.state.FzSelectionState
import Ir.co.tfs.farazaman.presentation.design.theme.FzSelectionMode
import org.junit.Assert.assertEquals
import org.junit.Test

class FzChipSetTest {
    @Test fun toggleModeAllowsDeselection() {
        val selected = FzSelectionState(FzSelectionMode.Toggle, listOf("a"), setOf("a"))
        assertEquals(emptySet<String>(), selected.toggle("a").selectedValues)
    }

    @Test fun noneModeNeverChangesSelection() {
        val state = FzSelectionState(FzSelectionMode.None, listOf("a"))
        assertEquals(state, state.toggle("a"))
    }
}
