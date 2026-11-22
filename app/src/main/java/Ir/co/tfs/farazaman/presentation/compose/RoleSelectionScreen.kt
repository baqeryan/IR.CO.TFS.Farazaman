package Ir.co.tfs.farazaman.presentation.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.presentation.design.components.FzBackButton
import Ir.co.tfs.farazaman.presentation.design.components.FzBottomActionBar
import Ir.co.tfs.farazaman.presentation.design.components.FzButtonVariant
import Ir.co.tfs.farazaman.presentation.design.theme.FzPersianFontFamily
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens

enum class RoleOption { SUPERVISOR, DRIVER, CONTRACTOR }

data class RoleSelectionUiState(
    val showSupervisor: Boolean = true,
    val showDriver: Boolean = true,
    val showContractor: Boolean = false,
    val selectedRole: RoleOption = RoleOption.SUPERVISOR,
    val showBack: Boolean = false,
) {
    val isEnterEnabled: Boolean get() = showSupervisor || showDriver || showContractor
}

sealed interface RoleSelectionEvent {
    data class SelectRole(val role: RoleOption) : RoleSelectionEvent
    data object Back : RoleSelectionEvent
    data object OpenProfile : RoleSelectionEvent
    data object OpenNotifications : RoleSelectionEvent
    data object Logout : RoleSelectionEvent
    data object Enter : RoleSelectionEvent
}

@Composable
fun RoleSelectionScreen(
    state: RoleSelectionUiState,
    unreadCount: Int = 0,
    onEvent: (RoleSelectionEvent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.supervisor_header_green)),
    ) {
        Image(
            painter = painterResource(R.drawable.home_header_curved),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            if (state.showBack) {
                FzBackButton(
                    onClick = { onEvent(RoleSelectionEvent.Back) },
                    modifier = Modifier.align(Alignment.TopStart),
                )
            } else {
                SupervisorNotificationAction(
                    unreadCount = unreadCount,
                    onClick = { onEvent(RoleSelectionEvent.OpenNotifications) },
                    modifier = Modifier.align(Alignment.TopStart),
                )
            }
            SupervisorProfileAction(
                onClick = { onEvent(RoleSelectionEvent.OpenProfile) },
                modifier = Modifier.align(Alignment.TopEnd),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(
                        if (state.isEnterEnabled) R.string.role_selection_title
                        else R.string.role_access_denied_title,
                    ),
                    color = FaraZamanThemeTokens.colors.onDecorative,
                    fontFamily = FzPersianFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        if (state.isEnterEnabled) R.string.role_selection_subtitle
                        else R.string.role_access_denied_subtitle,
                    ),
                    color = FaraZamanThemeTokens.colors.onDecorative.copy(alpha = 0.82f),
                    fontFamily = FzPersianFontFamily,
                    fontSize = 14.sp,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 196.dp)
                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(top = 28.dp, bottom = 20.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            ) {
                if (!state.isEnterEnabled) {
                    Spacer(Modifier.height(80.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_lock),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally).size(64.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.role_access_denied_message),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FzPersianFontFamily,
                        fontSize = 15.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                    Spacer(Modifier.height(80.dp))
                }
                if (state.showSupervisor) {
                    RoleCard(
                        title = stringResource(R.string.role_supervisor),
                        description = stringResource(R.string.role_supervisor_description),
                        icon = R.drawable.ic_role_supervisor,
                        selected = state.selectedRole == RoleOption.SUPERVISOR,
                        onClick = { onEvent(RoleSelectionEvent.SelectRole(RoleOption.SUPERVISOR)) },
                    )
                }
                if (state.showSupervisor && (state.showDriver || state.showContractor)) Spacer(Modifier.height(14.dp))
                if (state.showDriver) {
                    RoleCard(
                        title = stringResource(R.string.role_driver),
                        description = stringResource(R.string.role_driver_description),
                        icon = R.drawable.ic_role_driver,
                        selected = state.selectedRole == RoleOption.DRIVER,
                        onClick = { onEvent(RoleSelectionEvent.SelectRole(RoleOption.DRIVER)) },
                    )
                }
                if (state.showDriver && state.showContractor) Spacer(Modifier.height(14.dp))
                if (state.showContractor) {
                    RoleCard(
                        title = stringResource(R.string.role_contractor),
                        description = stringResource(R.string.role_contractor_description),
                        icon = R.drawable.ic_role_contractor,
                        selected = state.selectedRole == RoleOption.CONTRACTOR,
                        onClick = { onEvent(RoleSelectionEvent.SelectRole(RoleOption.CONTRACTOR)) },
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
            FzBottomActionBar(
                primaryText = stringResource(
                    if (state.isEnterEnabled) R.string.role_selection_enter
                    else R.string.role_access_denied_logout,
                ),
                onPrimaryClick = {
                    onEvent(if (state.isEnterEnabled) RoleSelectionEvent.Enter else RoleSelectionEvent.Logout)
                },
                primaryVariant = if (state.isEnterEnabled) FzButtonVariant.Primary else FzButtonVariant.Danger,
            )
        }
    }
}

@Composable
private fun RoleCard(title: String, description: String, icon: Int, selected: Boolean, onClick: () -> Unit) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    Card(
        modifier = Modifier.fillMaxWidth()
            .border(if (selected) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp),
                )
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(title, color = MaterialTheme.colorScheme.primary, fontFamily = FzPersianFontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(4.dp))
                Text(description, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f), fontFamily = FzPersianFontFamily, fontSize = 13.sp)
            }
            RoleSelectionIndicator(selected)
        }
    }
}

@Composable
private fun RoleSelectionIndicator(selected: Boolean) {
    Box(
        modifier = Modifier.size(22.dp).border(
            2.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            CircleShape,
        ).padding(6.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary, CircleShape))
    }
}

@Preview(
    name = "انتخاب نقش",
    showBackground = true,
    showSystemUi = true,
    locale = "fa",
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun RoleSelectionScreenPreview() {
    FaraZamanTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            RoleSelectionScreen(
                state = RoleSelectionUiState(
                    showSupervisor = true,
                    showDriver = true,
                    showContractor = true,
                    selectedRole = RoleOption.SUPERVISOR,
                ),
                unreadCount = 7,
                onEvent = {},
            )
        }
    }
}
