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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import Ir.co.tfs.farazaman.R

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

private val IranSans = FontFamily(Font(R.font.iran_sans))

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
            Row(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HeaderIcon(R.drawable.ic_user, R.string.supervisor_profile) { onEvent(RoleSelectionEvent.OpenProfile) }
                if (state.showBack) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        HeaderIcon(R.drawable.ic_arrow_back, R.string.back) { onEvent(RoleSelectionEvent.Back) }
                    }
                }
            }
            NotificationHeaderIcon(
                unreadCount = unreadCount,
                onClick = { onEvent(RoleSelectionEvent.OpenNotifications) },
                modifier = Modifier.align(Alignment.TopStart),
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
                    color = colorResource(R.color.white),
                    fontFamily = IranSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        if (state.isEnterEnabled) R.string.role_selection_subtitle
                        else R.string.role_access_denied_subtitle,
                    ),
                    color = colorResource(R.color.supervisor_header_subtitle),
                    fontFamily = IranSans,
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
                .padding(start = 20.dp, top = 28.dp, end = 20.dp, bottom = 20.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
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
                        fontFamily = IranSans,
                        fontSize = 15.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                    Spacer(Modifier.height(80.dp))
                }
                if (state.showSupervisor) {
                    RoleCard(
                        title = stringResource(R.string.role_supervisor),
                        description = stringResource(R.string.role_supervisor_description),
                        icon = R.drawable.ic_daily_tasks,
                        selected = state.selectedRole == RoleOption.SUPERVISOR,
                        onClick = { onEvent(RoleSelectionEvent.SelectRole(RoleOption.SUPERVISOR)) },
                    )
                }
                if (state.showSupervisor && (state.showDriver || state.showContractor)) Spacer(Modifier.height(14.dp))
                if (state.showDriver) {
                    RoleCard(
                        title = stringResource(R.string.role_driver),
                        description = stringResource(R.string.role_driver_description),
                        icon = R.drawable.ic_location,
                        selected = state.selectedRole == RoleOption.DRIVER,
                        onClick = { onEvent(RoleSelectionEvent.SelectRole(RoleOption.DRIVER)) },
                    )
                }
                if (state.showDriver && state.showContractor) Spacer(Modifier.height(14.dp))
                if (state.showContractor) {
                    RoleCard(
                        title = stringResource(R.string.role_contractor),
                        description = stringResource(R.string.role_contractor_description),
                        icon = R.drawable.ic_daily_tasks,
                        selected = state.selectedRole == RoleOption.CONTRACTOR,
                        onClick = { onEvent(RoleSelectionEvent.SelectRole(RoleOption.CONTRACTOR)) },
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
            Button(
                onClick = {
                    onEvent(if (state.isEnterEnabled) RoleSelectionEvent.Enter else RoleSelectionEvent.Logout)
                },
                enabled = true,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isEnterEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    contentColor = if (state.isEnterEnabled) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onError
                    },
                ),
            ) {
                Text(
                    stringResource(
                        if (state.isEnterEnabled) R.string.role_selection_enter
                        else R.string.role_access_denied_logout,
                    ),
                    fontFamily = IranSans,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

@Composable
private fun NotificationHeaderIcon(
    unreadCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        HeaderIcon(R.drawable.ic_notifications, R.string.supervisor_notifications, onClick)
        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(if (unreadCount > 9) 22.dp else 18.dp)
                    .background(MaterialTheme.colorScheme.error, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                    color = MaterialTheme.colorScheme.onError,
                    fontSize = if (unreadCount > 99) 8.sp else 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun HeaderIcon(icon: Int, description: Int, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = stringResource(description),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )
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
                Image(painterResource(icon), title, modifier = Modifier.size(32.dp))
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(title, color = MaterialTheme.colorScheme.primary, fontFamily = IranSans, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(4.dp))
                Text(description, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f), fontFamily = IranSans, fontSize = 13.sp)
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
