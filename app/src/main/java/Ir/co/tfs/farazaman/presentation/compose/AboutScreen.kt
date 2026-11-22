package Ir.co.tfs.farazaman.presentation.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzButtonVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzCard
import Ir.co.tfs.farazaman.presentation.design.components.FzCardVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzHeaderSize
import Ir.co.tfs.farazaman.presentation.design.components.FzPageTemplate
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens

data class AboutUiState(
    val versionName: String,
    val companyDescription: String,
    val websiteUrl: String = "https://tfs.co.ir/",
    val showDeveloperActions: Boolean = false,
)

sealed interface AboutEvent : UiEvent {
    data object Back : AboutEvent
    data object OpenWebsite : AboutEvent
    data object DebugInfo : AboutEvent
    data object ExportDatabase : AboutEvent
}

private data class AboutProject(val title: String, val description: String)

private val companyProjects = listOf(
    AboutProject(
        title = "فرازمان",
        description = "راهکار یکپارچه مدیریت عملیات میدانی، مأموریت‌ها، بازدیدها، نواقص و ثبت مسیر نیروهای اجرایی.",
    ),
    AboutProject(
        title = "نقشه باران",
        description = "زیرساخت نقشه و داده‌های مکانی برای نمایش معابر، محدوده‌های کاری و مسیرهای عملیاتی.",
    ),
    AboutProject(
        title = "راهکارهای خدمات شهری و پسماند",
        description = "سامانه‌های سازمانی برای برنامه‌ریزی، پایش و مدیریت فرایندهای خدمات شهری و محیط‌زیست.",
    ),
)

@Composable
fun AboutScreen(
    state: AboutUiState,
    onEvent: (AboutEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FaraZamanThemeTokens.spacing
    FzPageTemplate(
        title = stringResource(R.string.about),
        headerSize = FzHeaderSize.Standard,
        modifier = modifier,
        onBack = { onEvent(AboutEvent.Back) },
        headerBelowTitle = {
            Image(
                painter = painterResource(R.drawable.farazaman_log_),
                contentDescription = stringResource(R.string.login_logo_description),
                modifier = Modifier.size(FaraZamanThemeTokens.sizes.logoLarge),
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(horizontal = spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            FzCard(
                modifier = Modifier.fillMaxWidth(),
                variant = FzCardVariant.Elevated,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "فرازمان",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "سامانه مدیریت عملیات میدانی شهری",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "نسخه ${state.versionName}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            AboutSectionTitle("درباره شرکت")
            Text(
                text = state.companyDescription,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Justify,
            )

            AboutSectionTitle("پروژه‌ها و راهکارها")
            companyProjects.forEach { project ->
                FzCard(
                    modifier = Modifier.fillMaxWidth(),
                    variant = FzCardVariant.Outlined,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(spacing.md),
                        verticalArrangement = Arrangement.spacedBy(spacing.xs),
                    ) {
                        Text(
                            text = project.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = project.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            FzCard(
                modifier = Modifier.fillMaxWidth(),
                variant = FzCardVariant.Glass,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(spacing.md),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "وب‌سایت شرکت",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = state.websiteUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    FzButton(
                        text = "مشاهده وب‌سایت شرکت",
                        onClick = { onEvent(AboutEvent.OpenWebsite) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            if (state.showDeveloperActions) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    FzButton(
                        text = stringResource(R.string.about_debug_info),
                        variant = FzButtonVariant.Secondary,
                        onClick = { onEvent(AboutEvent.DebugInfo) },
                        modifier = Modifier.weight(1f),
                    )
                    FzButton(
                        text = stringResource(R.string.about_export_db),
                        variant = FzButtonVariant.Secondary,
                        onClick = { onEvent(AboutEvent.ExportDatabase) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutSectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, locale = "fa")
@Composable
private fun AboutScreenPreview() {
    FaraZamanTheme {
        AboutScreen(
            state = AboutUiState(
                versionName = "1.0",
                companyDescription = "تجارت فناوران سبز ارائه‌دهنده راهکارهای فناوری اطلاعات در حوزه محیط‌زیست و خدمات شهری است.",
            ),
            onEvent = {},
        )
    }
}
