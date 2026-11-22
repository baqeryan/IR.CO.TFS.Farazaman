package Ir.co.tfs.farazaman.presentation.design.catalog

import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzChipItem
import Ir.co.tfs.farazaman.presentation.design.components.FzChipSet
import Ir.co.tfs.farazaman.presentation.design.components.FzRating
import Ir.co.tfs.farazaman.presentation.design.theme.FzComponentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FzActionCatalog(modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf(setOf("today")) }
    var rating by remember { mutableStateOf(3f) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FzComponentSize.values().forEach { size ->
            FzButton(text = "دکمه ${size.name}", onClick = {}, size = size)
        }
        FzChipSet(
            items = listOf(FzChipItem("home", "خانه"), FzChipItem("today", "امروز"), FzChipItem("history", "تاریخچه")),
            selectedValues = selected,
            onSelectionChange = { selected = it },
        )
        FzRating(value = rating, onValueChange = { rating = it })
    }
}
