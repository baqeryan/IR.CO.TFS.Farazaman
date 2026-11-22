package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzButtonVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzIconButton
import Ir.co.tfs.farazaman.presentation.design.components.FzIconButtonVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzScaffold
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun SupervisorScaffold(
    selectedTab: SupervisorShellTab,
    onTabSelected: (SupervisorShellTab) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    FzScaffold(
        modifier = modifier,
        bottomBar = { SupervisorBottomNavigation(selectedTab, onTabSelected) },
        content = content,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SupervisorSwipePager(
    selectedTab: SupervisorShellTab,
    onTabSelected: (SupervisorShellTab) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (SupervisorShellTab) -> Unit,
) {
    val tabs = SupervisorShellTab.entries
    val pagerState = rememberPagerState(
        initialPage = selectedTab.ordinal,
        pageCount = { tabs.size },
    )

    LaunchedEffect(selectedTab) {
        val targetPage = selectedTab.ordinal
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }
    LaunchedEffect(pagerState) {
        // currentPage changes while a multi-page animation crosses Today. Publishing that
        // intermediate page updates selectedTab and cancels the original Home <-> History jump.
        // settledPage changes only after the swipe/animation has reached its real destination.
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                onTabSelected(tabs[page])
            }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier,
        key = { page -> tabs[page] },
    ) { page ->
        content(tabs[page])
    }
}

@Composable
fun SupervisorBottomNavigation(
    selectedTab: SupervisorShellTab,
    onTabSelected: (SupervisorShellTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FaraZamanThemeTokens.spacing
    Surface(
        modifier = modifier.navigationBarsPadding().padding(
            start = spacing.sm,
            top = spacing.sm,
            end = spacing.sm,
            bottom = spacing.xs,
        ),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = FaraZamanThemeTokens.elevation.overlay,
        shadowElevation = FaraZamanThemeTokens.elevation.overlay,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            NavigationItem("خانه", R.drawable.ic_supervisor_home, SupervisorShellTab.Home, selectedTab, onTabSelected, Modifier.weight(1f))
            NavigationItem("امروز", R.drawable.ic_supervisor_today, SupervisorShellTab.Today, selectedTab, onTabSelected, Modifier.weight(1f))
            NavigationItem("تاریخچه", R.drawable.ic_supervisor_history, SupervisorShellTab.History, selectedTab, onTabSelected, Modifier.weight(1f))
        }
    }
}

@Composable
private fun NavigationItem(
    label: String,
    icon: Int,
    tab: SupervisorShellTab,
    selectedTab: SupervisorShellTab,
    onTabSelected: (SupervisorShellTab) -> Unit,
    modifier: Modifier,
) {
    val selected = tab == selectedTab
    if (selected) {
        FzButton(
            text = label,
            onClick = { onTabSelected(tab) },
            modifier = modifier,
            variant = FzButtonVariant.Primary,
            leadingIcon = {
                Icon(painterResource(icon), contentDescription = null)
            },
            contentPadding = PaddingValues(
                horizontal = FaraZamanThemeTokens.spacing.sm,
                vertical = FaraZamanThemeTokens.spacing.none,
            ),
        )
    } else {
        Box(modifier, contentAlignment = Alignment.Center) {
            FzIconButton(
                contentDescription = label,
                onClick = { onTabSelected(tab) },
                variant = FzIconButtonVariant.Floating,
            ) {
                Icon(
                    painterResource(icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
