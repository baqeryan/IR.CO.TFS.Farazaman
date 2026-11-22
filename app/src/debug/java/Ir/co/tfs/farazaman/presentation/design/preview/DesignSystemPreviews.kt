package Ir.co.tfs.farazaman.presentation.design.preview

import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzButtonVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzCard
import Ir.co.tfs.farazaman.presentation.design.components.FzCardVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzEmptyState
import Ir.co.tfs.farazaman.presentation.design.components.FzErrorState
import Ir.co.tfs.farazaman.presentation.design.components.FzLoading
import Ir.co.tfs.farazaman.presentation.design.components.FzStatusChip
import Ir.co.tfs.farazaman.presentation.design.components.FzStatusVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzTextField
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
private fun ComponentCatalog() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FzButton("اصلی", {}, variant = FzButtonVariant.Primary)
        FzButton("ثانویه", {}, variant = FzButtonVariant.Secondary)
        FzButton("خطر", {}, variant = FzButtonVariant.Danger)
        FzButton("خالی", {}, variant = FzButtonVariant.Ghost)
        FzCard(variant = FzCardVariant.Outlined) { FzTextField("", {}, "توضیحات", singleLine = true) }
        FzStatusVariant.entries.forEach { FzStatusChip(it.name, variant = it) }
        FzLoading("در حال بارگذاری")
        FzEmptyState("موردی یافت نشد")
        FzErrorState("خطا", "دوباره تلاش کنید", "تلاش مجدد", {})
    }
}

@FzLightPreview
@Composable
fun LightComponentCatalogPreview() { FaraZamanTheme { ComponentCatalog() } }

@FzDarkPreview
@Composable
fun DarkComponentCatalogPreview() { FaraZamanTheme { ComponentCatalog() } }
