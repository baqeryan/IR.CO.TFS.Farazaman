package Ir.co.tfs.farazaman.presentation.compose

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.presentation.design.components.*
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens

data class ViolationDetailUiItem(val label: String, val value: String)

@Composable
fun ViolationDetailsSheet(title: String, subtitle: String, rows: List<ViolationDetailUiItem>, modifier: Modifier = Modifier) {
    val spacing = FaraZamanThemeTokens.spacing
    Column(modifier.fillMaxSize().padding(spacing.lg), verticalArrangement = Arrangement.spacedBy(spacing.md)) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        subtitle.takeIf(String::isNotBlank)?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        FzCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(spacing.md), verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                rows.forEach { row ->
                    Column(Modifier.fillMaxWidth()) {
                        Text(row.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(row.value.ifBlank { "-" }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ViolationImagesSheet(
    images: List<Uri>,
    onRemove: (Int) -> Unit,
    onDeleteAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FaraZamanThemeTokens.spacing
    Column(modifier.fillMaxSize().padding(spacing.lg), verticalArrangement = Arrangement.spacedBy(spacing.md)) {
        Text("تصاویر نقص", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("${images.size} تصویر بارگذاری شده", style = MaterialTheme.typography.bodyMedium)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            itemsIndexed(images, key = { index, uri -> "${uri}:$index" }) { index, uri ->
                FzCard {
                    Box(Modifier.aspectRatio(1f).fillMaxWidth()) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "تصویر ${index + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                        IconButton(onClick = { onRemove(index) }, modifier = Modifier.align(Alignment.TopStart)) {
                            Icon(painterResource(R.drawable.ic_close), contentDescription = "حذف تصویر", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
        FzButton("حذف همه تصاویر", onDeleteAll, modifier = Modifier.fillMaxWidth(), variant = FzButtonVariant.Danger)
    }
}
