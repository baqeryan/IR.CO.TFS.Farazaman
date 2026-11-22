package Ir.co.tfs.farazaman.presentation.design.catalog

import Ir.co.tfs.farazaman.presentation.design.components.FzAutocomplete
import Ir.co.tfs.farazaman.presentation.design.components.FzChoice
import Ir.co.tfs.farazaman.presentation.design.components.FzColorPicker
import Ir.co.tfs.farazaman.presentation.design.components.FzDatePicker
import Ir.co.tfs.farazaman.presentation.design.components.FzDropZone
import Ir.co.tfs.farazaman.presentation.design.components.FzForm
import Ir.co.tfs.farazaman.presentation.design.components.FzNumericField
import Ir.co.tfs.farazaman.presentation.design.components.FzPersianDate
import Ir.co.tfs.farazaman.presentation.design.components.FzMultiSelect
import Ir.co.tfs.farazaman.presentation.design.components.FzSelect
import Ir.co.tfs.farazaman.presentation.design.components.FzTextField
import Ir.co.tfs.farazaman.presentation.design.components.FzTime
import Ir.co.tfs.farazaman.presentation.design.components.FzTimePicker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun FzFormCatalog(modifier: Modifier = Modifier) {
    var name by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<String?>(null) }
    var multipleSelected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var color by remember { mutableStateOf(Color(0xFF00695C)) }
    var date by remember { mutableStateOf(FzPersianDate(1405, 7, 8)) }
    var time by remember { mutableStateOf(FzTime(8, 30)) }
    val roads = listOf(FzChoice("main", "خیابان اصلی"), FzChoice("side", "کوچه فرعی"))
    FzForm(modifier) {
        FzTextField(name, { name = it }, "نام معبر", required = true)
        FzNumericField(width, { width = it }, "عرض معبر", allowDecimal = true)
        FzAutocomplete(query, { query = it }, roads, { selected = it }, "جستجوی معبر")
        FzSelect(selected, roads, { selected = it }, "نوع معبر")
        FzMultiSelect(multipleSelected, roads, { multipleSelected = it }, "معابر")
        FzColorPicker(color, { color = it })
        FzDatePicker(date, { date = it })
        FzTimePicker(time, { time = it })
        FzDropZone({}, title = "افزودن تصویر", supportingText = "فقط تصاویر دوربین")
    }
}
