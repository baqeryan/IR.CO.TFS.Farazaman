package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzButtonVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzCard
import Ir.co.tfs.farazaman.presentation.design.components.FzCardVariant
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class IntroPageUi(
    val title: String,
    val description: String,
    @DrawableRes val illustration: Int,
    val tips: List<String> = emptyList(),
)

data class IntroUiState(
    val roleTitle: String,
    val pages: List<IntroPageUi>,
    val currentPage: Int = 0,
) {
    val isFirstPage: Boolean get() = currentPage == 0
    val isLastPage: Boolean get() = currentPage == pages.lastIndex
}

sealed interface IntroUiEvent : UiEvent {
    data object Previous : IntroUiEvent
    data object Next : IntroUiEvent
    data object Skip : IntroUiEvent
}

@Composable
fun IntroScreen(
    state: IntroUiState,
    onEvent: (IntroUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FaraZamanThemeTokens.spacing
    val page = state.pages.getOrNull(state.currentPage) ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = spacing.lg, vertical = spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "راهنمای شروع",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "نقش ${state.roleTitle}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (!state.isLastPage) {
                FzButton(
                    text = "رد کردن آموزش",
                    onClick = { onEvent(IntroUiEvent.Skip) },
                    variant = FzButtonVariant.Secondary,
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(page.illustration),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp, max = 245.dp)
                    .padding(vertical = spacing.md),
                contentScale = ContentScale.Fit,
            )
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = page.description,
                modifier = Modifier.padding(top = spacing.sm),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (page.tips.isNotEmpty()) {
                Spacer(Modifier.height(spacing.md))
                FzCard(
                    modifier = Modifier.fillMaxWidth(),
                    variant = FzCardVariant.Outlined,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(spacing.md),
                        verticalArrangement = Arrangement.spacedBy(spacing.sm),
                    ) {
                        page.tips.forEach { tip ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Box(
                                    Modifier
                                        .padding(top = 7.dp)
                                        .size(7.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                                )
                                Text(
                                    text = tip,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(spacing.md))
        }

        Text(
            text = "مرحله ${state.currentPage + 1} از ${state.pages.size}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.padding(vertical = spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            state.pages.indices.forEach { index ->
                Box(
                    Modifier
                        .size(if (index == state.currentPage) 12.dp else 8.dp)
                        .background(
                            if (index == state.currentPage) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape,
                        ),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            if (!state.isFirstPage) {
                FzButton(
                    text = "قبلی",
                    onClick = { onEvent(IntroUiEvent.Previous) },
                    modifier = Modifier.weight(1f),
                    variant = FzButtonVariant.Secondary,
                )
            }
            FzButton(
                text = if (state.isLastPage) "ورود به محیط کار" else "بعدی",
                onClick = { onEvent(IntroUiEvent.Next) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
