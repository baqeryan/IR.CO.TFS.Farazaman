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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import Ir.co.tfs.farazaman.R

enum class RoleOption {
    SUPERVISOR,
    DRIVER,
}

data class RoleSelectionUiState(
    val showSupervisor: Boolean = true,
    val showDriver: Boolean = true,
    val selectedRole: RoleOption = RoleOption.SUPERVISOR,
    val showBack: Boolean = false,
) {
    val isEnterEnabled: Boolean
        get() = showSupervisor || showDriver
}

private val IranSans = FontFamily(Font(R.font.iran_sans))
@Composable
fun RoleSelectionScreen(
    state: RoleSelectionUiState,
    onRoleSelected: (RoleOption) -> Unit,
    onBack: () -> Unit,
    onProfile: () -> Unit,
    onEnter: () -> Unit,
) {
    Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    if (state.showBack) {
                        HeaderIcon(R.drawable.ic_arrow_back, R.string.back, onBack)
                    } else {
                        Spacer(Modifier.size(44.dp))
                    }
                    HeaderIcon(R.drawable.ic_user, R.string.supervisor_profile, onProfile)
                }
                Text(
                    text = stringResource(R.string.role_selection_title),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontFamily = IranSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.role_selection_subtitle),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.86f),
                    fontFamily = IranSans,
                    fontSize = 14.sp,
                )
                Spacer(Modifier.height(24.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(20.dp),
            ) {
                if (state.showSupervisor) {
                    RoleCard(
                        title = stringResource(R.string.role_supervisor),
                        description = stringResource(R.string.role_supervisor_description),
                        icon = R.drawable.intro_clipboard,
                        selected = state.selectedRole == RoleOption.SUPERVISOR,
                        onClick = { onRoleSelected(RoleOption.SUPERVISOR) },
                    )
                }
                if (state.showSupervisor && state.showDriver) Spacer(Modifier.height(14.dp))
                if (state.showDriver) {
                    RoleCard(
                        title = stringResource(R.string.role_driver),
                        description = stringResource(R.string.role_driver_description),
                        icon = R.drawable.ic_location,
                        selected = state.selectedRole == RoleOption.DRIVER,
                        onClick = { onRoleSelected(RoleOption.DRIVER) },
                    )
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = onEnter,
                    enabled = state.isEnterEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(
                        stringResource(R.string.role_selection_enter),
                        fontFamily = IranSans,
                        fontSize = 16.sp,
                    )
                }
            }
    }
}

@Composable
private fun HeaderIcon(icon: Int, description: Int, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface),
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
private fun RoleCard(
    title: String,
    description: String,
    icon: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(if (selected) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(icon),
                    contentDescription = title,
                    modifier = Modifier.size(32.dp),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(title, color = MaterialTheme.colorScheme.primary, fontFamily = IranSans, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(4.dp))
                Text(description, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f), fontFamily = IranSans, fontSize = 13.sp)
            }
            RoleSelectionIndicator(selected = selected)
        }
    }
}

@Composable
private fun RoleSelectionIndicator(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .border(
                width = 2.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = CircleShape,
            )
            .padding(6.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
            )
        }
    }
}
