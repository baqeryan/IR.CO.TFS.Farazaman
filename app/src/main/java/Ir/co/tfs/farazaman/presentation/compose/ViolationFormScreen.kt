package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzButtonVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzCard
import Ir.co.tfs.farazaman.presentation.design.components.FzChoice
import Ir.co.tfs.farazaman.presentation.design.components.FzNumericField
import Ir.co.tfs.farazaman.presentation.design.components.FzHeaderSize
import Ir.co.tfs.farazaman.presentation.design.components.FzPageTemplate
import Ir.co.tfs.farazaman.presentation.design.components.FzTextField
import Ir.co.tfs.farazaman.presentation.design.components.FzSelect
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import Ir.co.tfs.farazaman.presentation.design.theme.FzComponentSize
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

data class ViolationSelectorUiState(
    val label: String,
    val value: String? = null,
    val loading: Boolean = false,
    val enabled: Boolean = true,
    val selectedId: Int? = null,
    val options: List<ViolationSelectorOption> = emptyList(),
)

data class ViolationSelectorOption(val id: Int, val label: String)

data class ViolationFormUiState(
    val title: String = "ثبت اطلاعات نقص",
    val selectors: List<ViolationSelectorUiState> = emptyList(),
    val count: String = "",
    val address: String = "",
    val description: String = "",
    val imageCount: Int = 0,
    val images: List<Uri> = emptyList(),
    val gpsWarning: String? = null,
    val fieldError: String? = null,
    val submitting: Boolean = false,
    val loading: Boolean = false,
)

sealed interface ViolationFormEvent {
    data object Back : ViolationFormEvent
    data class SelectOption(val index: Int, val value: Int? = null) : ViolationFormEvent
    data class CountChanged(val value: String) : ViolationFormEvent
    data class AddressChanged(val value: String) : ViolationFormEvent
    data class DescriptionChanged(val value: String) : ViolationFormEvent
    data object CaptureImage : ViolationFormEvent
    data class RemoveImage(val index: Int) : ViolationFormEvent
    data object Submit : ViolationFormEvent
}

sealed interface ViolationFormMutation {
    data class CountChanged(val value: String) : ViolationFormMutation
    data class AddressChanged(val value: String) : ViolationFormMutation
    data class DescriptionChanged(val value: String) : ViolationFormMutation
    data class ImageCountChanged(val count: Int) : ViolationFormMutation
    data class ValidationFailed(val message: String) : ViolationFormMutation
    data class SubmittingChanged(val submitting: Boolean) : ViolationFormMutation
}

object ViolationFormReducer {
    fun reduce(state: ViolationFormUiState, mutation: ViolationFormMutation) = when (mutation) {
        is ViolationFormMutation.CountChanged -> state.copy(
            count = normalizeViolationNumericInput(mutation.value),
            fieldError = null,
        )
        is ViolationFormMutation.AddressChanged -> state.copy(address = mutation.value, fieldError = null)
        is ViolationFormMutation.DescriptionChanged -> state.copy(description = mutation.value, fieldError = null)
        is ViolationFormMutation.ImageCountChanged -> state.copy(imageCount = mutation.count.coerceAtLeast(0))
        is ViolationFormMutation.ValidationFailed -> state.copy(fieldError = mutation.message, submitting = false)
        is ViolationFormMutation.SubmittingChanged -> state.copy(submitting = mutation.submitting)
    }
}

internal fun normalizeViolationNumericInput(input: String): String = buildString(input.length) {
    input.forEach { character ->
        character.digitToIntOrNull()?.let(::append)
    }
}

@Composable
fun ViolationFormScreen(
    state: ViolationFormUiState,
    onEvent: (ViolationFormEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    FzPageTemplate(title = state.title, headerSize = FzHeaderSize.Compact, modifier = modifier, onBack = { onEvent(ViolationFormEvent.Back) }) { padding ->
        ViolationFormContent(
            state = state,
            onEvent = onEvent,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
fun ViolationFormContent(
    state: ViolationFormUiState,
    onEvent: (ViolationFormEvent) -> Unit,
    modifier: Modifier = Modifier,
    topContentPadding: Dp = 0.dp,
    bottomContentPadding: Dp = 0.dp,
) {
    val spacing = FaraZamanThemeTokens.spacing
    val sizes = FaraZamanThemeTokens.sizes
    val imagePreviewSize = sizes.logoLarge + spacing.md
    Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = topContentPadding, bottom = bottomContentPadding),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            state.selectors.forEachIndexed { index, selector ->
                FzSelect(
                    value = selector.selectedId,
                    options = selector.options.map { FzChoice(it.id, it.label) },
                    onValueChange = { onEvent(ViolationFormEvent.SelectOption(index, it)) },
                    label = selector.label,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selector.enabled && !selector.loading,
                    loading = selector.loading,
                )
            }

            FzNumericField(
                value = state.count,
                onValueChange = { onEvent(ViolationFormEvent.CountChanged(it)) },
                label = "تعداد یا مقدار",
            )
            FzTextField(
                value = state.address,
                onValueChange = { onEvent(ViolationFormEvent.AddressChanged(it)) },
                label = "آدرس",
            )
            FzTextField(
                value = state.description,
                onValueChange = { onEvent(ViolationFormEvent.DescriptionChanged(it)) },
                label = "توضیحات (اختیاری)",
                errorText = state.fieldError,
            )

            state.gpsWarning?.let {
                Text(it, color = FaraZamanThemeTokens.semanticColors.warning)
            }

            FzCard {
                Column(
                    Modifier.fillMaxWidth().padding(spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("تصاویر (${state.imageCount})", style = MaterialTheme.typography.titleSmall)
                        FzButton(
                            "افزودن تصویر",
                            { onEvent(ViolationFormEvent.CaptureImage) },
                            variant = FzButtonVariant.Ghost,
                            size = FzComponentSize.Small,
                        )
                    }
                    if (state.images.isEmpty()) {
                        Text(
                            "هنوز تصویری ثبت نشده است.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth().height(imagePreviewSize),
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        ) {
                            itemsIndexed(state.images, key = { index, uri -> "$uri:$index" }) { index, uri ->
                                Box(
                                    Modifier.width(imagePreviewSize).height(imagePreviewSize)
                                        .clip(MaterialTheme.shapes.medium),
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "تصویر ${index + 1}",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                    IconButton(
                                        onClick = { onEvent(ViolationFormEvent.RemoveImage(index)) },
                                        modifier = Modifier.align(Alignment.TopStart).padding(spacing.xs)
                                            .size(sizes.iconLarge).background(
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                                CircleShape,
                                            ),
                                    ) {
                                        Icon(
                                            painterResource(R.drawable.ic_delete),
                                            contentDescription = "حذف تصویر",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(sizes.iconSmall),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            FzButton(
                "ثبت نقص",
                { onEvent(ViolationFormEvent.Submit) },
                modifier = Modifier.fillMaxWidth(),
                loading = state.submitting,
            )
        }
}
