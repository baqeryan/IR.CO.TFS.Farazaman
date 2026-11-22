package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.R
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import Ir.co.tfs.farazaman.presentation.design.components.*
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import Ir.co.tfs.farazaman.presentation.design.theme.FzComponentSize

enum class SupervisorTodayTab { Planning, Continuous, Commitments }

data class SupervisorTaskUiItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val violationCount: Int = 0,
    val canRegisterViolation: Boolean = true,
    val billSystemCode: String = "",
)

data class SupervisorTodayUiState(
    val selectedTab: SupervisorTodayTab = SupervisorTodayTab.Planning,
    val planningItems: List<SupervisorTaskUiItem> = emptyList(),
    val continuousItems: List<SupervisorTaskUiItem> = emptyList(),
    val tracking: Boolean = false,
    val currentStreet: String? = null,
    val currentRoadWidth: String? = null,
    val unreadNotifications: Int = 0,
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val commitmentForm: ViolationFormUiState = ViolationFormUiState(title = "ثبت نقص"),
) {
    val visibleItems: List<SupervisorTaskUiItem>
        get() = if (selectedTab == SupervisorTodayTab.Planning) planningItems else continuousItems
}

sealed interface SupervisorTodayEvent {
    data class SelectTab(val tab: SupervisorTodayTab) : SupervisorTodayEvent
    data class OpenMap(val id: String) : SupervisorTodayEvent
    data class RegisterViolation(val id: String) : SupervisorTodayEvent
    data object StartVisit : SupervisorTodayEvent
    data object OpenActiveVisitMap : SupervisorTodayEvent
    data object OpenNotifications : SupervisorTodayEvent
    data object OpenProfile : SupervisorTodayEvent
    data object OpenHome : SupervisorTodayEvent
    data object OpenHistory : SupervisorTodayEvent
    data object Retry : SupervisorTodayEvent
    data class CommitmentForm(val event: ViolationFormEvent) : SupervisorTodayEvent
}

@Composable
fun SupervisorTodayScreen(
    state: SupervisorTodayUiState,
    onEvent: (SupervisorTodayEvent) -> Unit,
    modifier: Modifier = Modifier,
    bottomContentPadding: Dp = 0.dp,
) {
    val spacing = FaraZamanThemeTokens.spacing
    val swipeThresholdPx = with(LocalDensity.current) { 64.dp.toPx() }
    var horizontalDrag by remember(state.selectedTab) { mutableFloatStateOf(0f) }
    var planningSearchQuery by rememberSaveable { mutableStateOf("") }
    var continuousSearchQuery by rememberSaveable { mutableStateOf("") }
    FzPageTemplate(
        title = "برنامه امروز",
        subtitle = listOfNotNull(
            state.currentStreet?.takeIf(String::isNotBlank)?.let { "نام معبر: $it" },
            state.currentRoadWidth?.takeIf(String::isNotBlank)?.let { "عرض معبر: $it" },
        ).joinToString("\n"),
        headerSize = FzHeaderSize.Compact,
        headerStart = {
            SupervisorNotificationAction(
                unreadCount = state.unreadNotifications,
                onClick = { onEvent(SupervisorTodayEvent.OpenNotifications) },
            )
        },
        headerEnd = { SupervisorProfileAction(onClick = { onEvent(SupervisorTodayEvent.OpenProfile) }) },
        modifier = modifier,
    ) { _ ->
        Column(
            Modifier.fillMaxSize().padding(
                start = spacing.sm,
                top = spacing.sm,
                end = spacing.sm,
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            FzCard(
                modifier = Modifier.fillMaxWidth(),
                variant = FzCardVariant.Glass,
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(spacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                ) {
                    TodayTab("برنامه‌ریزی", SupervisorTodayTab.Planning, state, onEvent, Modifier.weight(1f))
                    TodayTab("مستمر", SupervisorTodayTab.Continuous, state, onEvent, Modifier.weight(1f))
                    TodayTab("نواقص", SupervisorTodayTab.Commitments, state, onEvent, Modifier.weight(1f))
                }
            }
            val visitBarHeight = FaraZamanThemeTokens.sizes.buttonHeight(FzComponentSize.Small) + spacing.sm
            val visitContentTop = visitBarHeight + spacing.sm
            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(state.selectedTab, swipeThresholdPx) {
                        detectHorizontalDragGestures(
                            onDragStart = { horizontalDrag = 0f },
                            onHorizontalDrag = { change, dragAmount ->
                                horizontalDrag += dragAmount
                                change.consume()
                            },
                            onDragCancel = { horizontalDrag = 0f },
                            onDragEnd = {
                                val tabs = SupervisorTodayTab.entries
                                when {
                                    horizontalDrag <= -swipeThresholdPx &&
                                        state.selectedTab == SupervisorTodayTab.Planning -> {
                                        onEvent(SupervisorTodayEvent.OpenHome)
                                    }
                                    horizontalDrag >= swipeThresholdPx &&
                                        state.selectedTab == SupervisorTodayTab.Commitments -> {
                                        onEvent(SupervisorTodayEvent.OpenHistory)
                                    }
                                    horizontalDrag >= swipeThresholdPx -> {
                                        val target = state.selectedTab.ordinal + 1
                                        if (target in tabs.indices) {
                                            onEvent(SupervisorTodayEvent.SelectTab(tabs[target]))
                                        }
                                    }
                                    horizontalDrag <= -swipeThresholdPx -> {
                                        val target = state.selectedTab.ordinal - 1
                                        if (target in tabs.indices) {
                                            onEvent(SupervisorTodayEvent.SelectTab(tabs[target]))
                                        }
                                    }
                                }
                                horizontalDrag = 0f
                            },
                        )
                    },
            ) {
                AnimatedContent(
                    targetState = state.selectedTab,
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = {
                        val movingForward = targetState.ordinal > initialState.ordinal
                        if (movingForward) {
                            slideInHorizontally(tween(260)) { -it } togetherWith
                                slideOutHorizontally(tween(260)) { it }
                        } else {
                            slideInHorizontally(tween(260)) { it } togetherWith
                                slideOutHorizontally(tween(260)) { -it }
                        }
                    },
                    label = "today-tab-content",
                ) { animatedTab ->
                    val sourceItems = when (animatedTab) {
                        SupervisorTodayTab.Planning -> state.planningItems
                        SupervisorTodayTab.Continuous -> state.continuousItems
                        SupervisorTodayTab.Commitments -> emptyList()
                    }
                    val searchQuery = when (animatedTab) {
                        SupervisorTodayTab.Planning -> planningSearchQuery
                        SupervisorTodayTab.Continuous -> continuousSearchQuery
                        SupervisorTodayTab.Commitments -> ""
                    }.trim()
                    val animatedItems = if (searchQuery.isNotEmpty()) {
                        sourceItems.filter { item ->
                            item.title.contains(searchQuery, ignoreCase = true) ||
                                item.subtitle?.contains(searchQuery, ignoreCase = true) == true ||
                                item.billSystemCode.contains(searchQuery, ignoreCase = true)
                        }
                    } else {
                        sourceItems
                    }
                when {
                    state.loading -> FzLoading("در حال دریافت برنامه امروز...")
                    state.errorMessage != null -> Column(
                        Modifier.padding(top = visitContentTop),
                    ) {
                        FzErrorState("خطا در دریافت برنامه", state.errorMessage)
                        FzButton("تلاش مجدد", { onEvent(SupervisorTodayEvent.Retry) })
                    }
                    animatedTab == SupervisorTodayTab.Commitments -> ViolationFormContent(
                        state = state.commitmentForm,
                        onEvent = { onEvent(SupervisorTodayEvent.CommitmentForm(it)) },
                        modifier = Modifier.fillMaxSize(),
                        topContentPadding = visitContentTop,
                        bottomContentPadding = bottomContentPadding + spacing.sm,
                    )
                    else -> LazyColumn(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(spacing.sm),
                        contentPadding = PaddingValues(
                            top = visitContentTop,
                            bottom = bottomContentPadding + spacing.sm,
                        ),
                    ) {
                        if (animatedTab != SupervisorTodayTab.Commitments) {
                            item(key = "${animatedTab.name}-search") {
                                FzTextField(
                                    value = if (animatedTab == SupervisorTodayTab.Planning) {
                                        planningSearchQuery
                                    } else {
                                        continuousSearchQuery
                                    },
                                    onValueChange = { query ->
                                        if (animatedTab == SupervisorTodayTab.Planning) {
                                            planningSearchQuery = query
                                        } else {
                                            continuousSearchQuery = query
                                        }
                                    },
                                    label = if (animatedTab == SupervisorTodayTab.Planning) {
                                        "جستجو در برنامه‌ریزی"
                                    } else {
                                        "جستجو در مستمر"
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                )
                            }
                        }
                        if (animatedItems.isEmpty()) {
                            item(key = "empty-search-result") {
                                FzEmptyState(
                                    title = if (searchQuery.isEmpty()) "موردی ثبت نشده" else "نتیجه‌ای یافت نشد",
                                    message = if (searchQuery.isEmpty()) {
                                        "برای این بخش برنامه‌ای ثبت نشده است."
                                    } else {
                                        "عنوان، شرح یا شماره ردیف دیگری را جستجو کنید."
                                    },
                                )
                            }
                        }
                        items(animatedItems, key = { it.id }) { item ->
                            FzCard(variant = FzCardVariant.Outlined) {
                                Column(Modifier.fillMaxWidth().padding(spacing.md), verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                                        Column(Modifier.weight(1f)) {
                                            item.billSystemCode.takeIf(String::isNotBlank)?.let { code ->
                                                Text(
                                                    text = "شماره ردیف فهرست بها: $code",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.primary,
                                                )
                                            }
                                            Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            item.subtitle?.takeIf(String::isNotBlank)?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                        }
                                        FzStatusChip(
                                            if (item.violationCount > 0) "${item.violationCount} نقص ثبت شده" else "موردی ثبت نشده",
                                            variant = if (item.violationCount > 0) FzStatusVariant.Warning else FzStatusVariant.Default,
                                        )
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                                        FzButton("نمایش روی نقشه", { onEvent(SupervisorTodayEvent.OpenMap(item.id)) }, modifier = Modifier.weight(1f), variant = FzButtonVariant.Ghost)
                                        FzButton("ثبت نقص", { onEvent(SupervisorTodayEvent.RegisterViolation(item.id)) }, modifier = Modifier.weight(1f), variant = FzButtonVariant.Secondary, enabled = item.canRegisterViolation)
                                    }
                                }
                            }
                        }
                    }
                }
                }
                if (state.tracking) {
                    ActiveVisitGlassBar(
                        onOpenMap = { onEvent(SupervisorTodayEvent.OpenActiveVisitMap) },
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                } else {
                    InactiveVisitAlertBar(
                        onStartVisit = { onEvent(SupervisorTodayEvent.StartVisit) },
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                }
            }
        }
    }
}

@Composable
private fun InactiveVisitAlertBar(onStartVisit: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = FaraZamanThemeTokens.spacing
    val sizes = FaraZamanThemeTokens.sizes
    FzCard(
        modifier = modifier.fillMaxWidth(),
        variant = FzCardVariant.GlassAlert,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = spacing.sm, vertical = spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_nav_schedule),
                contentDescription = null,
                modifier = Modifier.size(sizes.iconMedium),
                tint = FaraZamanThemeTokens.semanticColors.warning,
            )
            Column(Modifier.weight(1f)) {
                Text(
                    "بازدید شروع نشده!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "برای ثبت نقص ابتدا بازدید را شروع کنید.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            FzButton(
                "شروع بازدید",
                onStartVisit,
                modifier = Modifier.height(sizes.minimumTouchTarget - spacing.sm),
                variant = FzButtonVariant.Ghost,
                size = FzComponentSize.Small,
                contentPadding = PaddingValues(horizontal = spacing.sm, vertical = spacing.none),
            )
        }
    }
}

@Composable
private fun ActiveVisitGlassBar(onOpenMap: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = FaraZamanThemeTokens.spacing
    val sizes = FaraZamanThemeTokens.sizes
    FzCard(
        modifier = modifier.fillMaxWidth(),
        variant = FzCardVariant.GlassSuccess,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = spacing.sm, vertical = spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_route_recording),
                contentDescription = null,
                modifier = Modifier.size(sizes.iconMedium),
                tint = FaraZamanThemeTokens.semanticColors.success,
            )
            Column(Modifier.weight(1f)) {
                Text(
                    "بازدید فعال",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "بازدید در حال انجام است",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            FzButton(
                "نمایش روی نقشه",
                onOpenMap,
                modifier = Modifier.height(sizes.minimumTouchTarget - spacing.sm),
                variant = FzButtonVariant.Ghost,
                size = FzComponentSize.Small,
                contentPadding = PaddingValues(horizontal = spacing.sm, vertical = spacing.none),
            )
        }
    }
}

@Composable
private fun TodayTab(label: String, tab: SupervisorTodayTab, state: SupervisorTodayUiState, onEvent: (SupervisorTodayEvent) -> Unit, modifier: Modifier) {
    FzButton(
        label,
        { onEvent(SupervisorTodayEvent.SelectTab(tab)) },
        modifier = modifier,
        variant = if (state.selectedTab == tab) FzButtonVariant.Primary else FzButtonVariant.Ghost,
        size = FzComponentSize.Small,
        contentPadding = PaddingValues(horizontal = FaraZamanThemeTokens.spacing.xs, vertical = FaraZamanThemeTokens.spacing.none),
    )
}
