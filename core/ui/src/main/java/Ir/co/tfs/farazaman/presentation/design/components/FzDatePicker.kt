package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Immutable
data class FzPersianDate(val year: Int, val month: Int, val day: Int) : Comparable<FzPersianDate> {
    init { require(year in 1200..1600); require(month in 1..12); require(day in 1..31) }
    override fun compareTo(other: FzPersianDate): Int = compareValuesBy(this, other, FzPersianDate::year, FzPersianDate::month, FzPersianDate::day)
    override fun toString(): String = "%04d/%02d/%02d".format(year, month, day)
}

@Composable
fun FzDatePicker(
    value: FzPersianDate,
    onValueChange: (FzPersianDate) -> Unit,
    modifier: Modifier = Modifier,
    minimum: FzPersianDate = FzPersianDate(1300, 1, 1),
    maximum: FzPersianDate = FzPersianDate(1500, 12, 31),
) {
    require(minimum <= maximum)
    FzField("تاریخ", Ir.co.tfs.farazaman.presentation.design.state.FzFieldState(), modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(FaraZamanThemeTokens.spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            FzDatePart("سال", value.year, { next -> onValueChange(value.copy(year = next).coerceIn(minimum, maximum)) }, minimum.year, maximum.year)
            FzDatePart("ماه", value.month, { next -> onValueChange(value.copy(month = next).coerceIn(minimum, maximum)) }, 1, 12)
            FzDatePart("روز", value.day, { next -> onValueChange(value.copy(day = next).coerceIn(minimum, maximum)) }, 1, 31)
        }
    }
}

@Composable
private fun FzDatePart(label: String, value: Int, onChange: (Int) -> Unit, minimum: Int, maximum: Int) =
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FzButton("+", { onChange((value + 1).coerceAtMost(maximum)) }, variant = FzButtonVariant.Ghost)
        Text(value.toString(), style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelSmall)
        FzButton("−", { onChange((value - 1).coerceAtLeast(minimum)) }, variant = FzButtonVariant.Ghost)
    }

@Composable
fun FzDateRangePicker(
    start: FzPersianDate,
    end: FzPersianDate,
    onRangeChange: (FzPersianDate, FzPersianDate) -> Unit,
    modifier: Modifier = Modifier,
) = Column(modifier, verticalArrangement = Arrangement.spacedBy(FaraZamanThemeTokens.spacing.md)) {
    FzDatePicker(start, { onRangeChange(it, if (it > end) it else end) })
    FzDatePicker(end, { onRangeChange(start, if (it < start) start else it) }, minimum = start)
}

private fun FzPersianDate.coerceIn(minimum: FzPersianDate, maximum: FzPersianDate): FzPersianDate = when {
    this < minimum -> minimum
    this > maximum -> maximum
    else -> this
}
