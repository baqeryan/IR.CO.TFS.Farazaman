package Ir.co.tfs.farazaman.presentation.design.catalog

import Ir.co.tfs.farazaman.presentation.design.components.FzTopBar
import Ir.co.tfs.farazaman.presentation.design.components.FzTabItem
import Ir.co.tfs.farazaman.presentation.design.components.FzTabs
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun FzCatalogScreen(onBack: (() -> Unit)? = null) {
    var section by remember { mutableStateOf(fzCatalogDestinations.first().id) }
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            FzTopBar(title = "کاتالوگ رابط فرازمان", onBack = onBack)
            FzTabs(
                items = fzCatalogDestinations.map { FzTabItem(it.id, it.title) },
                selectedId = section,
                onSelected = { section = it },
            )
            val contentModifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(FaraZamanThemeTokens.spacing.md)
            requireNotNull(fzCatalogDestinations.find { it.id == section }) {
                "Unknown Fz catalog section: $section"
            }.content(contentModifier)
        }
    }
}
