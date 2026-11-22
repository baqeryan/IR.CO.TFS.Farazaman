package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzButtonVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzCard
import Ir.co.tfs.farazaman.presentation.design.components.FzCardVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzHeaderSize
import Ir.co.tfs.farazaman.presentation.design.components.FzIconButton
import Ir.co.tfs.farazaman.presentation.design.components.FzIconButtonVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzPageTemplate
import Ir.co.tfs.farazaman.presentation.design.components.FzStatusChip
import Ir.co.tfs.farazaman.presentation.design.components.FzStatusVariant
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class SupervisorProfileUiState(
    val displayName: String,
    val userName: String?,
    val roleName: String?,
    val canChangeRole: Boolean,
    val mail: String? = null,
)
sealed interface SupervisorProfileEvent { data object Back : SupervisorProfileEvent; data object ChangeRole : SupervisorProfileEvent; data object Logout : SupervisorProfileEvent }

sealed interface SupervisorProfileMutation {
    data class Loaded(
        val displayName: String,
        val userName: String?,
        val roleName: String?,
        val canChangeRole: Boolean,
        val mail: String? = null,
    ) : SupervisorProfileMutation
}

object SupervisorProfileReducer {
    fun reduce(state: SupervisorProfileUiState, mutation: SupervisorProfileMutation) = when (mutation) {
        is SupervisorProfileMutation.Loaded -> state.copy(
            displayName = mutation.displayName,
            userName = mutation.userName,
            mail = mutation.mail,
            roleName = mutation.roleName,
            canChangeRole = mutation.canChangeRole,
        )
    }
}

@Composable
fun SupervisorProfileScreen(state: SupervisorProfileUiState, onEvent: (SupervisorProfileEvent) -> Unit, modifier: Modifier = Modifier) {
    val spacing = FaraZamanThemeTokens.spacing
    FzPageTemplate(
        title = "پروفایل کاربری",
        subtitle = "اطلاعات حساب و دسترسی‌ها",
        headerSize = FzHeaderSize.Compact,
        modifier = modifier,
        onBack = { onEvent(SupervisorProfileEvent.Back) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            ProfileIdentityCard(state)

            Text(
                text = "اطلاعات حساب",
                modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.xs),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            FzCard(
                modifier = Modifier.fillMaxWidth(),
                variant = FzCardVariant.Outlined,
            ) {
                ProfileInfoRow(
                    icon = R.drawable.ic_user,
                    label = "نام کاربری",
                    value = state.userName?.takeIf(String::isNotBlank) ?: "ثبت نشده",
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = spacing.md),
                    color = FaraZamanThemeTokens.colors.uiBorder,
                )
                ProfileInfoRow(
                    icon = R.drawable.ic_user,
                    label = "ایمیل",
                    value = state.mail?.takeIf(String::isNotBlank) ?: "ثبت نشده",
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = spacing.md),
                    color = FaraZamanThemeTokens.colors.uiBorder,
                )
                ProfileInfoRow(
                    icon = R.drawable.ic_role_supervisor,
                    label = "نقش فعال",
                    value = state.roleName?.takeIf(String::isNotBlank) ?: "تعیین نشده",
                )
            }

            Text(
                text = "مدیریت حساب",
                modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.xs),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FzButton(
                    text = "خروج از حساب",
                    onClick = { onEvent(SupervisorProfileEvent.Logout) },
                    variant = FzButtonVariant.Danger,
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(painterResource(R.drawable.ic_logout), contentDescription = null)
                    },
                )
                if (state.canChangeRole) {
                    FzIconButton(
                        contentDescription = "تغییر نقش",
                        onClick = { onEvent(SupervisorProfileEvent.ChangeRole) },
                        variant = FzIconButtonVariant.Floating,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_switch_role),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileIdentityCard(state: SupervisorProfileUiState) {
    val spacing = FaraZamanThemeTokens.spacing
    FzCard(
        modifier = Modifier.fillMaxWidth(),
        variant = FzCardVariant.Glass,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Surface(
                modifier = Modifier.size(84.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = state.displayName.trim().firstOrNull()?.toString() ?: "ف",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Text(
                text = state.displayName,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            state.roleName?.takeIf(String::isNotBlank)?.let { role ->
                FzStatusChip(text = role, variant = FzStatusVariant.Success)
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(icon: Int, label: String, value: String) {
    val spacing = FaraZamanThemeTokens.spacing
    Row(
        modifier = Modifier.fillMaxWidth().padding(spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
