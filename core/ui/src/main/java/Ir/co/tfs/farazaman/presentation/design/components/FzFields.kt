package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.state.FzFieldState
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier

@Immutable
data class FzFormError(val field: String, val message: String)

@Composable
fun FzForm(
    modifier: Modifier = Modifier,
    errors: List<FzFormError> = emptyList(),
    content: @Composable ColumnScope.() -> Unit,
) = Column(modifier, verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(FaraZamanThemeTokens.spacing.md)) {
    content()
    if (errors.isNotEmpty()) {
        Column(Modifier.fillMaxWidth()) {
            errors.forEach { Text("${it.field}: ${it.message}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
fun FzField(
    label: String,
    state: FzFieldState,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    content: @Composable () -> Unit,
) = Column(modifier, verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(FaraZamanThemeTokens.spacing.xs)) {
    Text(if (state.required) "$label *" else label, style = MaterialTheme.typography.labelLarge)
    content()
    (state.errorMessage ?: supportingText)?.let {
        Text(it, color = if (state.errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}
