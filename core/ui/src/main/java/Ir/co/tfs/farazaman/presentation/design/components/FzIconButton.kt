package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.core.ui.R
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import Ir.co.tfs.farazaman.presentation.design.theme.FzComponentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics

enum class FzIconButtonVariant { Header, Floating, Ghost, Danger }

@Composable
fun FzIconButton(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: FzIconButtonVariant = FzIconButtonVariant.Floating,
    enabled: Boolean = true,
    size: FzComponentSize = FzComponentSize.Medium,
    icon: @Composable () -> Unit,
) {
    val container = when (variant) {
        FzIconButtonVariant.Header -> MaterialTheme.colorScheme.surface
        FzIconButtonVariant.Floating -> MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
        FzIconButtonVariant.Ghost -> Color.Transparent
        FzIconButtonVariant.Danger -> MaterialTheme.colorScheme.errorContainer
    }
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(FaraZamanThemeTokens.sizes.buttonHeight(size))
            .semantics { role = Role.Button; this.contentDescription = contentDescription },
        enabled = enabled,
        shape = CircleShape,
        color = container,
        tonalElevation = if (variant == FzIconButtonVariant.Floating) FaraZamanThemeTokens.spacing.xs else FaraZamanThemeTokens.spacing.none,
        shadowElevation = if (variant == FzIconButtonVariant.Floating) FaraZamanThemeTokens.spacing.xs else FaraZamanThemeTokens.spacing.none,
    ) {
        Box(contentAlignment = Alignment.Center) { icon() }
    }
}

@Composable
fun FzBackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FzIconButton(
        contentDescription = "بازگشت",
        onClick = onClick,
        modifier = modifier,
        variant = FzIconButtonVariant.Header,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_arrow_back),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(FaraZamanThemeTokens.sizes.iconMedium),
        )
    }
}
