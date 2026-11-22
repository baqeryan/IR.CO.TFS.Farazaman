package Ir.co.tfs.farazaman.presentation.design.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier

@Immutable
data class FzCatalogSection(
    val id: String,
    val title: String,
    val examples: List<FzCatalogExample>,
)

@Immutable
data class FzCatalogExample(
    val id: String,
    val title: String,
    val description: String? = null,
    val content: @Composable () -> Unit,
)

@Immutable
data class FzCatalogDestination(
    val id: String,
    val title: String,
    val content: @Composable (Modifier) -> Unit,
)

internal val fzCatalogDestinations = listOf(
    FzCatalogDestination("actions", "کنترل‌ها", { FzActionCatalog(it) }),
    FzCatalogDestination("layout", "چیدمان", { FzLayoutCatalog(it) }),
    FzCatalogDestination("forms", "فرم‌ها", { FzFormCatalog(it) }),
    FzCatalogDestination("feedback", "پیام‌ها", { FzFeedbackCatalog(it) }),
    FzCatalogDestination("data", "داده‌ها", { FzDataCatalog(it) }),
).also { destinations ->
    val expected = setOf("actions", "layout", "forms", "feedback", "data")
    require(destinations.map { it.id }.toSet() == expected) { "Fz catalog sections are incomplete" }
    require(destinations.distinctBy { it.id }.size == destinations.size) { "Fz catalog section ids must be unique" }
}
