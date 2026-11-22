package Ir.co.tfs.farazaman.presentation.design.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

fun interface FzInputMask {
    fun filter(input: String): String
}

object FzInputMasks {
    val Digits = FzInputMask { it.filter(Char::isDigit) }
    val Decimal = FzInputMask { input ->
        var separatorSeen = false
        input.filter { char -> char.isDigit() || ((char == '.' || char == '٫') && !separatorSeen.also { separatorSeen = true }) }
    }
    fun maxLength(length: Int) = FzInputMask { it.take(length.coerceAtLeast(0)) }
}

@Composable
fun FzNumericField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    allowDecimal: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    required: Boolean = false,
    errorText: String? = null,
) = FzTextField(
    value = value,
    onValueChange = { onValueChange((if (allowDecimal) FzInputMasks.Decimal else FzInputMasks.Digits).filter(it)) },
    label = label,
    modifier = modifier,
    enabled = enabled,
    readOnly = readOnly,
    required = required,
    errorText = errorText,
    singleLine = true,
    keyboardOptions = KeyboardOptions(keyboardType = if (allowDecimal) KeyboardType.Decimal else KeyboardType.Number),
)
