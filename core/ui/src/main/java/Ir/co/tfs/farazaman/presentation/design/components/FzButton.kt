package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import Ir.co.tfs.farazaman.presentation.design.theme.FzComponentSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow

enum class FzButtonVariant { Primary, Secondary, Danger, Ghost }

@Composable
fun FzButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: FzButtonVariant = FzButtonVariant.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    size: FzComponentSize = FzComponentSize.Medium,
) {
    val shape = RoundedCornerShape(percent = 50)
    val colors = FaraZamanThemeTokens.colors
    val sized = modifier.defaultMinSize(minHeight = FaraZamanThemeTokens.sizes.buttonHeight(size))
        .semantics { role = Role.Button }
    val content: @Composable () -> Unit = {
        Row(horizontalArrangement = Arrangement.Center) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(FaraZamanThemeTokens.sizes.iconSize(size)),
                    strokeWidth = FaraZamanThemeTokens.spacing.xs,
                )
                Spacer(Modifier.size(FaraZamanThemeTokens.spacing.sm))
            } else if (leadingIcon != null) {
                androidx.compose.foundation.layout.Box(Modifier.size(FaraZamanThemeTokens.sizes.iconSize(size))) { leadingIcon() }
                Spacer(Modifier.size(FaraZamanThemeTokens.spacing.sm))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!loading && trailingIcon != null) {
                Spacer(Modifier.size(FaraZamanThemeTokens.spacing.sm))
                androidx.compose.foundation.layout.Box(Modifier.size(FaraZamanThemeTokens.sizes.iconSize(size))) { trailingIcon() }
            }
        }
    }
    when (variant) {
        FzButtonVariant.Primary -> Button(
            onClick = onClick,
            modifier = sized.clip(shape).fzHorizontalGradient(
                if (enabled && !loading) colors.interactivePrimary else colors.interactiveSecondary,
            ),
            enabled = enabled && !loading,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = colors.onDecorative,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = colors.textSecondary,
            ),
            contentPadding = contentPadding,
            content = { content() },
        )
        FzButtonVariant.Secondary -> OutlinedButton(
            onClick = onClick,
            modifier = sized,
            enabled = enabled && !loading,
            shape = shape,
            contentPadding = contentPadding,
            content = { content() },
        )
        FzButtonVariant.Danger -> Button(
            onClick, sized, enabled = enabled && !loading, shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ), contentPadding = contentPadding, content = { content() },
        )
        FzButtonVariant.Ghost -> TextButton(
            onClick,
            sized,
            enabled = enabled && !loading,
            shape = shape,
            contentPadding = contentPadding,
            content = { content() },
        )
    }
}
