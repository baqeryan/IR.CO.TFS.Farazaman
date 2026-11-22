package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

@Immutable
data class FzFileItem(val id: String, val name: String, val sizeBytes: Long? = null, val mimeType: String? = null)

@Composable
fun FzFilePicker(
    files: List<FzFileItem>,
    onPick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    buttonText: String = "انتخاب فایل",
    onRemove: ((FzFileItem) -> Unit)? = null,
) = Column(modifier, verticalArrangement = Arrangement.spacedBy(FaraZamanThemeTokens.spacing.sm)) {
    FzButton(buttonText, onPick, enabled = enabled)
    files.forEach { file ->
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(file.name, Modifier.weight(1f), maxLines = 1)
            if (onRemove != null) FzButton("حذف", { onRemove(file) }, variant = FzButtonVariant.Ghost, enabled = enabled)
        }
    }
}

@Composable
fun FzDropZone(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    title: String = "برای انتخاب فایل لمس کنید",
    supportingText: String? = null,
) = Surface(
    modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick),
    shape = RoundedCornerShape(FaraZamanThemeTokens.spacing.md),
    border = BorderStroke(FaraZamanThemeTokens.spacing.xs / 2, MaterialTheme.colorScheme.outline),
    color = MaterialTheme.colorScheme.surfaceVariant,
) {
    Column(Modifier.fillMaxWidth().padding(FaraZamanThemeTokens.spacing.lg), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        supportingText?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
    }
}
