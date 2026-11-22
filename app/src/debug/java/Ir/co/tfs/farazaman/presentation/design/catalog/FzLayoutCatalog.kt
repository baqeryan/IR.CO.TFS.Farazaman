package Ir.co.tfs.farazaman.presentation.design.catalog

import Ir.co.tfs.farazaman.presentation.design.components.FzBottomNavigation
import Ir.co.tfs.farazaman.presentation.design.components.FzExpansionPanel
import Ir.co.tfs.farazaman.presentation.design.components.FzGrid
import Ir.co.tfs.farazaman.presentation.design.components.FzGridItem
import Ir.co.tfs.farazaman.presentation.design.components.FzList
import Ir.co.tfs.farazaman.presentation.design.components.FzListItem
import Ir.co.tfs.farazaman.presentation.design.components.FzNavigationItem
import Ir.co.tfs.farazaman.presentation.design.components.FzSurface
import Ir.co.tfs.farazaman.presentation.design.components.FzTabs
import Ir.co.tfs.farazaman.presentation.design.components.FzTabItem
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FzLayoutCatalog(modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf("today") }
    val navigation = listOf(
        FzNavigationItem("home", "خانه"),
        FzNavigationItem("today", "امروز"),
        FzNavigationItem("history", "تاریخچه"),
    )
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FzGrid(2, listOf(
            FzGridItem { FzSurface(Modifier.fillMaxWidth()) { Text("ستون اول") } },
            FzGridItem { FzSurface(Modifier.fillMaxWidth()) { Text("ستون دوم") } },
        ))
        FzExpansionPanel("جزئیات بازدید", expanded, { expanded = it }) { Text("محتوای پنل") }
        FzList { FzListItem("برنامه امروز", subtitle = "خیابان نمونه، عرض ۱۲ متر") }
        FzTabs(navigation.map { FzTabItem(it.id, it.label) }, selected, { selected = it })
        FzBottomNavigation(navigation, selected, { selected = it }) { item, _ -> Text(item.label.take(1)) }
    }
}
