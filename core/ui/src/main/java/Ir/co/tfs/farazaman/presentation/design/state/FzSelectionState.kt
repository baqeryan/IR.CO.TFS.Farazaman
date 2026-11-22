package Ir.co.tfs.farazaman.presentation.design.state

import Ir.co.tfs.farazaman.presentation.design.theme.FzSelectionMode
import androidx.compose.runtime.Immutable

@Immutable
data class FzSelectionState<T>(
    val mode: FzSelectionMode,
    val availableValues: List<T>,
    val selectedValues: Set<T> = emptySet(),
    val enabled: Boolean = true,
    val readOnly: Boolean = false,
) {
    init {
        require(selectedValues.all(availableValues::contains)) { "Selected values must be available" }
        require(mode == FzSelectionMode.Multiple || selectedValues.size <= 1) {
            "Only multiple selection may contain more than one selected value"
        }
    }

    fun toggle(value: T): FzSelectionState<T> {
        if (!enabled || readOnly || value !in availableValues || mode == FzSelectionMode.None) return this
        val selected = value in selectedValues
        val next = when (mode) {
            FzSelectionMode.None -> selectedValues
            FzSelectionMode.Single -> setOf(value)
            FzSelectionMode.Toggle -> if (selected) emptySet() else setOf(value)
            FzSelectionMode.Multiple -> if (selected) selectedValues - value else selectedValues + value
        }
        return copy(selectedValues = next)
    }
}

