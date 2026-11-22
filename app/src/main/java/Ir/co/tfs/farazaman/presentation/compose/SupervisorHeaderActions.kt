package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.presentation.design.components.FzIconButton
import Ir.co.tfs.farazaman.presentation.design.components.FzIconButtonVariant
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight

@Composable
fun SupervisorNotificationAction(
    unreadCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FaraZamanThemeTokens.spacing
    val sizes = FaraZamanThemeTokens.sizes
    Box(modifier) {
        FzIconButton(
            contentDescription = "اعلان‌ها",
            onClick = onClick,
            variant = FzIconButtonVariant.Header,
        ) {
            Icon(
                painterResource(R.drawable.ic_notifications),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(FaraZamanThemeTokens.sizes.iconMedium),
            )
        }
        if (unreadCount > 0) {
            Box(
                Modifier.align(Alignment.TopEnd).offset(x = spacing.xs, y = -spacing.xs)
                    .size(if (unreadCount > 9) sizes.iconMedium else sizes.iconSmall)
                    .background(MaterialTheme.colorScheme.error, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (unreadCount > 99) "99+" else unreadCount.toString(),
                    color = MaterialTheme.colorScheme.onError,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
fun SupervisorProfileAction(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FzIconButton(
        contentDescription = "پروفایل",
        onClick = onClick,
        modifier = modifier,
        variant = FzIconButtonVariant.Header,
    ) {
        Icon(
            painterResource(R.drawable.ic_user),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(FaraZamanThemeTokens.sizes.iconMedium),
        )
    }
}
