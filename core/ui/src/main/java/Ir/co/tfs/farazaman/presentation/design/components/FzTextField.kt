package Ir.co.tfs.farazaman.presentation.design.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun FzTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = false,
    errorText: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    readOnly: Boolean = false,
    required: Boolean = false,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(if (required) "$label *" else label) },
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        isError = errorText != null,
        supportingText = (errorText ?: supportingText)?.let { text -> { Text(text) } },
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        leadingIcon = leadingIcon?.let { { Box { it() } } },
        trailingIcon = trailingIcon?.let { { Box { it() } } },
        modifier = modifier.fillMaxWidth().then(
            if (errorText == null) Modifier else Modifier.semantics { error(errorText) },
        ),
    )
}
