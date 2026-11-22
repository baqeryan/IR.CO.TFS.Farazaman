package Ir.co.tfs.farazaman.presentation.design.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Immutable
data class FzChoice<T>(
    val value: T,
    val label: String,
    val enabled: Boolean = true,
    val supportingText: String? = null,
)

@Composable
fun <T> FzSelect(
    value: T?,
    options: List<FzChoice<T>>,
    onValueChange: (T) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    errorText: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val text = options.firstOrNull { it.value == value }?.label.orEmpty()
    val filteredOptions = remember(searchQuery, options) {
        val query = searchQuery.trim()
        if (query.isEmpty()) options else options.filter {
            it.label.contains(query, ignoreCase = true) ||
                it.supportingText?.contains(query, ignoreCase = true) == true
        }
    }

    androidx.compose.foundation.layout.Box(modifier) {
        FzTextField(
            value = text,
            onValueChange = {},
            label = label,
            modifier = Modifier,
            enabled = enabled,
            singleLine = true,
            errorText = errorText,
            readOnly = true,
            trailingIcon = {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(if (expanded) "▲" else "▼")
                }
            },
        )
        androidx.compose.foundation.layout.Box(
            Modifier
                .matchParentSize()
                .clip(MaterialTheme.shapes.extraSmall)
                .clickable(enabled = enabled && !loading && options.isNotEmpty()) {
                    searchQuery = ""
                    expanded = true
                },
        )
    }

    if (expanded) {
        FzThreeStageBottomSheet(
            onDismissRequest = {
                expanded = false
                searchQuery = ""
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 16.dp),
            ) {
                Text("انتخاب $label", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                FzTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "جستجو",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))

                if (filteredOptions.isEmpty()) {
                    Text(
                        text = "نتیجه‌ای یافت نشد",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 32.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                    ) {
                        items(filteredOptions) { option ->
                            FzCard(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                variant = if (option.value == value) FzCardVariant.Selected else FzCardVariant.Standard,
                                onClick = if (option.enabled) {
                                    {
                                        expanded = false
                                        searchQuery = ""
                                        onValueChange(option.value)
                                    }
                                } else null,
                            ) {
                                FzChoiceContent(option, Modifier.padding(16.dp))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun <T> FzMultiSelect(
    values: Set<T>,
    options: List<FzChoice<T>>,
    onValuesChange: (Set<T>) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    errorText: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var pendingValues by remember { mutableStateOf(values) }
    val selectedText = options.filter { it.value in values }.joinToString("، ") { it.label }
    val filteredOptions = remember(searchQuery, options) {
        val query = searchQuery.trim()
        if (query.isEmpty()) options else options.filter {
            it.label.contains(query, ignoreCase = true) ||
                it.supportingText?.contains(query, ignoreCase = true) == true
        }
    }

    androidx.compose.foundation.layout.Box(modifier) {
        FzTextField(
            value = selectedText,
            onValueChange = {},
            label = label,
            modifier = Modifier,
            enabled = enabled,
            singleLine = true,
            errorText = errorText,
            readOnly = true,
            trailingIcon = { Text(if (expanded) "▲" else "▼") },
        )
        androidx.compose.foundation.layout.Box(
            Modifier
                .matchParentSize()
                .clip(MaterialTheme.shapes.extraSmall)
                .clickable(enabled = enabled && options.isNotEmpty()) {
                    pendingValues = values
                    searchQuery = ""
                    expanded = true
                },
        )
    }

    if (expanded) {
        FzThreeStageBottomSheet(
            onDismissRequest = {
                expanded = false
                searchQuery = ""
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 16.dp),
            ) {
                Text("انتخاب $label", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                FzTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "جستجو",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))

                if (filteredOptions.isEmpty()) {
                    Text(
                        text = "نتیجه‌ای یافت نشد",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 32.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                    ) {
                        items(filteredOptions) { option ->
                            val selected = option.value in pendingValues
                            FzCard(
                                modifier = Modifier.fillMaxWidth(),
                                variant = if (selected) FzCardVariant.Selected else FzCardVariant.Standard,
                                onClick = if (option.enabled) {
                                    {
                                        pendingValues = if (selected) {
                                            pendingValues - option.value
                                        } else {
                                            pendingValues + option.value
                                        }
                                    }
                                } else null,
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(
                                        checked = selected,
                                        onCheckedChange = null,
                                        enabled = option.enabled,
                                    )
                                    FzChoiceContent(option, Modifier.padding(vertical = 12.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                FzButton(
                    text = "انتخاب",
                    onClick = {
                        onValuesChange(pendingValues)
                        expanded = false
                        searchQuery = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun FzThreeStageBottomSheet(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val scope = rememberCoroutineScope()
            val fullHeightPx = with(density) { maxHeight.toPx() }
            val fullAnchor = 0f
            val threeQuarterAnchor = fullHeightPx * 0.25f
            val halfAnchor = fullHeightPx * 0.5f
            val dismissThreshold = fullHeightPx * 0.68f
            val offsetY = remember(fullHeightPx) { Animatable(threeQuarterAnchor) }
            val visibleSheetHeight = with(density) {
                (fullHeightPx - offsetY.value).coerceAtLeast(0f).toDp()
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
                    .clickable(onClick = onDismissRequest),
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(visibleSheetHeight)
                    .offset { IntOffset(0, offsetY.value.roundToInt()) },
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 1.dp,
            ) {
                Column(Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .pointerInput(fullHeightPx) {
                                detectVerticalDragGestures(
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        scope.launch {
                                            offsetY.snapTo(
                                                (offsetY.value + dragAmount).coerceIn(
                                                    fullAnchor,
                                                    fullHeightPx,
                                                ),
                                            )
                                        }
                                    },
                                    onDragEnd = {
                                        if (offsetY.value >= dismissThreshold) {
                                            onDismissRequest()
                                        } else {
                                            val target = listOf(
                                                fullAnchor,
                                                threeQuarterAnchor,
                                                halfAnchor,
                                            ).minBy { anchor -> kotlin.math.abs(anchor - offsetY.value) }
                                            scope.launch { offsetY.animateTo(target, spring()) }
                                        }
                                    },
                                    onDragCancel = {
                                        val target = listOf(
                                            fullAnchor,
                                            threeQuarterAnchor,
                                            halfAnchor,
                                        ).minBy { anchor -> kotlin.math.abs(anchor - offsetY.value) }
                                        scope.launch { offsetY.animateTo(target, spring()) }
                                    },
                                )
                            }
                            .semantics { contentDescription = "دستگیره تغییر ارتفاع پنجره انتخاب" },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            Modifier
                                .width(80.dp)
                                .height(7.dp)
                                .clip(MaterialTheme.shapes.extraLarge)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)),
                        )
                    }
                    Box(Modifier.fillMaxWidth().weight(1f)) { content() }
                }
            }
        }
        }
    }
}

@Composable
private fun <T> FzChoiceContent(
    option: FzChoice<T>,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            text = option.label,
            color = if (option.enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            style = MaterialTheme.typography.bodyLarge,
        )
        option.supportingText?.takeIf { it.isNotBlank() }?.let { supportingText ->
            Text(
                text = supportingText,
                color = if (option.enabled) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun <T> FzAutocomplete(
    query: String,
    onQueryChange: (String) -> Unit,
    options: List<FzChoice<T>>,
    onSelected: (T) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    minimumQueryLength: Int = 1,
) {
    var expanded by remember { mutableStateOf(false) }
    val filtered = remember(query, options) { options.filter { it.label.contains(query, ignoreCase = true) } }
    androidx.compose.foundation.layout.Box(modifier) {
        FzTextField(query, { onQueryChange(it); expanded = it.length >= minimumQueryLength }, label)
        DropdownMenu(expanded && filtered.isNotEmpty(), { expanded = false }) {
            filtered.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        expanded = false
                        onSelected(option.value)
                    },
                    enabled = option.enabled,
                )
            }
        }
    }
}
