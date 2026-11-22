package Ir.co.tfs.farazaman.presentation.compose

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzButtonVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzCard
import Ir.co.tfs.farazaman.presentation.design.components.FzCardVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzHeaderSize
import Ir.co.tfs.farazaman.presentation.design.components.FzPageTemplate
import Ir.co.tfs.farazaman.presentation.design.components.FzStatusChip
import Ir.co.tfs.farazaman.presentation.design.components.FzStatusVariant
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import Ir.co.tfs.farazaman.presentation.design.theme.FzComponentSize

data class SupervisorHomeUiState(
    val greeting: String = "",
    val date: String = "",
    val missionTitle: String? = null,
    val tracking: Boolean = false,
    val insideWorkArea: Boolean? = null,
    val dailyTasks: Int = 0,
    val continuousTasks: Int = 0,
    val violations: Int = 0,
    val unreadNotifications: Int = 0,
    val backgroundLocationGranted: Boolean = true,
    val batteryOptimizationIgnored: Boolean = true,
)

sealed interface SupervisorHomeEvent {
    data object StartVisit : SupervisorHomeEvent
    data object StopVisit : SupervisorHomeEvent
    data object EndVisit : SupervisorHomeEvent
    data object OpenMap : SupervisorHomeEvent
    data object OpenNotifications : SupervisorHomeEvent
    data object OpenProfile : SupervisorHomeEvent
    data object ChangeWorkArea : SupervisorHomeEvent
    data object OpenDailyTasks : SupervisorHomeEvent
    data object OpenContinuousTasks : SupervisorHomeEvent
    data object OpenViolations : SupervisorHomeEvent
    data object RequestBackgroundLocation : SupervisorHomeEvent
    data object OpenBatteryOptimizationSettings : SupervisorHomeEvent
}

@Composable
fun SupervisorHomeScreen(
    state: SupervisorHomeUiState,
    onEvent: (SupervisorHomeEvent) -> Unit,
    modifier: Modifier = Modifier,
    bottomContentPadding: Dp = 0.dp,
) {
    val spacing = FaraZamanThemeTokens.spacing
    FzPageTemplate(
        title = "فرازمان",
        subtitle = listOf(state.greeting, state.date).filter(String::isNotBlank).joinToString("  •  "),
        headerSize = FzHeaderSize.Compact,
        headerStart = {
            SupervisorNotificationAction(
                unreadCount = state.unreadNotifications,
                onClick = { onEvent(SupervisorHomeEvent.OpenNotifications) },
            )
        },
        headerEnd = {
            SupervisorProfileAction(onClick = { onEvent(SupervisorHomeEvent.OpenProfile) })
        },
        modifier = modifier,
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(bottom = bottomContentPadding + spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            if (!state.backgroundLocationGranted) {
                BackgroundLocationWarning(
                    onRequestPermission = {
                        onEvent(SupervisorHomeEvent.RequestBackgroundLocation)
                    },
                )
            }
            if (!state.batteryOptimizationIgnored) {
                BatteryOptimizationWarning(
                    onOpenSettings = {
                        onEvent(SupervisorHomeEvent.OpenBatteryOptimizationSettings)
                    },
                )
            }

            WorkAreaCard(
                missionTitle = state.missionTitle,
                onChange = { onEvent(SupervisorHomeEvent.ChangeWorkArea) },
            )

            VisitHeroCard(state = state, onEvent = onEvent)

            Text(
                text = "خلاصه امروز",
                modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.xs),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                HomeStat(
                    title = "وظایف روزانه",
                    count = state.dailyTasks,
                    icon = R.drawable.ic_daily_tasks,
                    modifier = Modifier.weight(1f),
                    onClick = { onEvent(SupervisorHomeEvent.OpenDailyTasks) },
                )
                HomeStat(
                    title = "وظایف مستمر",
                    count = state.continuousTasks,
                    icon = R.drawable.ic_continuous_tasks,
                    modifier = Modifier.weight(1f),
                    onClick = { onEvent(SupervisorHomeEvent.OpenContinuousTasks) },
                )
                HomeStat(
                    title = "نواقص ثبت‌شده",
                    count = state.violations,
                    icon = R.drawable.ic_registered_violation,
                    modifier = Modifier.weight(1f),
                    onClick = { onEvent(SupervisorHomeEvent.OpenViolations) },
                )
            }
        }
    }
}

@Composable
private fun BatteryOptimizationWarning(onOpenSettings: () -> Unit) {
    val spacing = FaraZamanThemeTokens.spacing
    FzCard(modifier = Modifier.fillMaxWidth(), variant = FzCardVariant.GlassAlert) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            HomeIconBubble(R.drawable.ic_gps_fix, compact = true)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Text(
                    "اجرای پس‌زمینه محدود است",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Text(
                    "برای جلوگیری از توقف ثبت مسیر، مصرف باتری فرازمان را روی «بدون محدودیت» قرار دهید.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FzButton(
                text = "تنظیمات",
                onClick = onOpenSettings,
                variant = FzButtonVariant.Ghost,
                size = FzComponentSize.Small,
            )
        }
    }
}

@Composable
private fun BackgroundLocationWarning(onRequestPermission: () -> Unit) {
    val spacing = FaraZamanThemeTokens.spacing
    FzCard(
        modifier = Modifier.fillMaxWidth(),
        variant = FzCardVariant.GlassAlert,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            HomeIconBubble(R.drawable.ic_location, compact = true)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                Text(
                    text = "ردیابی در پس‌زمینه محدود است",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "برای ثبت مطمئن مسیر، دسترسی موقعیت مکانی را روی «همیشه مجاز» قرار دهید. تا قبل از فعال‌سازی، برنامه را باز نگه دارید و صفحه دستگاه را خاموش نکنید.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FzButton(
                text = "فعال‌سازی",
                onClick = onRequestPermission,
                variant = FzButtonVariant.Ghost,
                size = FzComponentSize.Small,
            )
        }
    }
}

@Composable
private fun WorkAreaCard(missionTitle: String?, onChange: () -> Unit) {
    val spacing = FaraZamanThemeTokens.spacing
    FzCard(modifier = Modifier.fillMaxWidth(), variant = FzCardVariant.Outlined) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            HomeIconBubble(R.drawable.ic_location)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Text(
                    text = "محدوده کاری",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = missionTitle ?: "محدوده‌ای انتخاب نشده است",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FzButton(
                text = "تغییر",
                onClick = onChange,
                variant = FzButtonVariant.Ghost,
                size = FzComponentSize.Small,
            )
        }
    }
}

@Composable
private fun VisitHeroCard(
    state: SupervisorHomeUiState,
    onEvent: (SupervisorHomeEvent) -> Unit,
) {
    val spacing = FaraZamanThemeTokens.spacing
    FzCard(
        modifier = Modifier.fillMaxWidth(),
        variant = if (state.tracking) FzCardVariant.Glass else FzCardVariant.GlassAlert,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                HomeIconBubble(
                    icon = if (state.tracking) R.drawable.ic_route_recording else R.drawable.ic_location,
                    active = state.tracking,
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    Text(
                        text = if (state.tracking) "بازدید فعال" else "بازدید شروع نشده!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = if (state.tracking) {
                            "بازدید در حال انجام است و مسیر شما ثبت می‌شود."
                        } else {
                            "برای ثبت مسیر و نقص، ابتدا بازدید را شروع کنید."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (state.tracking) {
                    FzStatusChip("فعال", variant = FzStatusVariant.Success)
                }
            }

            state.insideWorkArea?.takeIf { state.tracking }?.let { inside ->
                FzStatusChip(
                    text = if (inside) "در محدوده کاری" else "خارج از محدوده کاری",
                    variant = if (inside) FzStatusVariant.Success else FzStatusVariant.Error,
                )
            }

            if (state.tracking) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    FzButton(
                        text = "نقشه",
                        onClick = { onEvent(SupervisorHomeEvent.OpenMap) },
                        modifier = Modifier.weight(1f),
                        variant = FzButtonVariant.Secondary,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_map),
                                contentDescription = null,
                            )
                        },
                    )
                    HoldVisitButton(
                        tracking = true,
                        onEvent = onEvent,
                        modifier = Modifier.weight(1f),
                    )
                }
                Text(
                    text = "برای ثبت و ارسال نهایی، بازدید را از گزینه زیر پایان دهید.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FzButton(
                    text = "پایان کامل و بارگذاری مأموریت",
                    onClick = { onEvent(SupervisorHomeEvent.EndVisit) },
                    modifier = Modifier.fillMaxWidth(),
                    variant = FzButtonVariant.Ghost,
                    size = FzComponentSize.Small,
                )
            } else {
                HoldVisitButton(
                    tracking = false,
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun HoldVisitButton(
    tracking: Boolean,
    onEvent: (SupervisorHomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        FzButton(
            text = if (tracking) "توقف بازدید" else "شروع بازدید",
            onClick = {
                onEvent(
                    if (tracking) SupervisorHomeEvent.StopVisit else SupervisorHomeEvent.StartVisit,
                )
            },
            modifier = Modifier.fillMaxWidth(),
            variant = if (tracking) FzButtonVariant.Danger else FzButtonVariant.Primary,
            size = if (tracking) FzComponentSize.Medium else FzComponentSize.Small,
            leadingIcon = {
                Icon(
                    painter = painterResource(
                        if (tracking) R.drawable.ic_stop_visit else R.drawable.ic_route_recording,
                    ),
                    contentDescription = null,
                )
            },
        )
    }
}

@Composable
private fun HomeStat(
    title: String,
    count: Int,
    @DrawableRes icon: Int,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val spacing = FaraZamanThemeTokens.spacing
    FzCard(modifier = modifier, variant = FzCardVariant.Elevated, onClick = onClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = spacing.md, horizontal = spacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            HomeIconBubble(icon = icon, compact = true)
            Text(
                text = title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                minLines = 2,
            )
            Text(
                text = count.toString(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "مورد",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HomeIconBubble(
    @DrawableRes icon: Int,
    active: Boolean = false,
    compact: Boolean = false,
) {
    val size = if (compact) 40.dp else 48.dp
    Surface(
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = if (active) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(if (compact) 22.dp else 26.dp),
            )
        }
    }
}
