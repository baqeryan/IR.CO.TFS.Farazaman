package Ir.co.tfs.farazaman.presentation.design.components

import org.junit.Assert.assertEquals
import org.junit.Test

class FzDataGridStateTest {
    private data class Row(val id: Int, val name: String)
    private val columns = listOf(FzTableColumn<Row>("name", "نام") { it.name })

    @Test fun filterSortAndPageAreDeterministic() {
        val rows = listOf(Row(1, "تهران"), Row(2, "تبریز"), Row(3, "شیراز"))
        val state = FzDataGridState<Int>(query = "ت", sortColumnId = "name", pageSize = 2)
        assertEquals(listOf("تبریز", "تهران"), state.resolveRows(rows, columns).map { it.name })
    }

    @Test fun selectionTogglesWithoutMutatingPreviousState() {
        val initial = FzDataGridState<Int>()
        val selected = initial.toggleSelection(2)
        assertEquals(emptySet<Int>(), initial.selectedKeys)
        assertEquals(setOf(2), selected.selectedKeys)
        assertEquals(emptySet<Int>(), selected.toggleSelection(2).selectedKeys)
    }
}
