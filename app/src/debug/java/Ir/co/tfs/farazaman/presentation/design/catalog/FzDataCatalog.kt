package Ir.co.tfs.farazaman.presentation.design.catalog

import Ir.co.tfs.farazaman.presentation.design.components.FzChart
import Ir.co.tfs.farazaman.presentation.design.components.FzChartPoint
import Ir.co.tfs.farazaman.presentation.design.components.FzChartType
import Ir.co.tfs.farazaman.presentation.design.components.FzDataGrid
import Ir.co.tfs.farazaman.presentation.design.components.FzDataGridState
import Ir.co.tfs.farazaman.presentation.design.components.FzTableColumn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class VisitRow(val id: Int, val road: String, val width: Int)

@Composable
fun FzDataCatalog(modifier: Modifier = Modifier) {
    val rows = remember { listOf(VisitRow(1, "خیابان آزادی", 20), VisitRow(2, "بلوار امام", 30), VisitRow(3, "کوچه بهار", 8)) }
    val columns = remember { listOf(
        FzTableColumn<VisitRow>("road", "نام معبر") { it.road },
        FzTableColumn<VisitRow>("width", "عرض") { it.width.toString() },
    ) }
    var state by remember { mutableStateOf(FzDataGridState<Int>()) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FzDataGrid(rows, columns, state, { it.id }, { state = it })
        FzChart(rows.map { FzChartPoint(it.road, it.width.toFloat()) }, FzChartType.Bar)
    }
}
