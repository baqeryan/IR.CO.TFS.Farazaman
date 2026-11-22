package Ir.co.tfs.farazaman.presentation.design.preview

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import Ir.co.tfs.farazaman.presentation.design.theme.FzPersianFontFamily
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
private fun TokenCatalog() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
        Text("رنگ اصلی", color = MaterialTheme.colorScheme.primary, fontFamily = FzPersianFontFamily)
        Text("عنوان و متن بدنه", style = MaterialTheme.typography.titleLarge)
        Text("فاصله‌ها: xs / sm / md / lg / xl")
    }
}

@FzLightPreview
@Composable
fun LightTokenCatalogPreview() { FaraZamanTheme { TokenCatalog() } }

@FzDarkPreview
@Composable
fun DarkTokenCatalogPreview() { FaraZamanTheme { TokenCatalog() } }
