package Ir.co.tfs.farazaman.presentation.design.components

import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FzDialog(
    visible: Boolean,
    title: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    dismissLabel: String? = null,
    dismissible: Boolean = true,
    content: (@Composable () -> Unit)? = null,
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = { if (dismissible) onDismiss() },
        modifier = modifier,
        title = { Text(title) },
        text = content ?: message?.let { text -> { Text(text) } },
        confirmButton = { FzButton(confirmLabel, onConfirm) },
        dismissButton = dismissLabel?.let { label -> { FzButton(label, onDismiss, variant = FzButtonVariant.Ghost) } },
    )
}

@Composable
fun FzBackConfirmation(
    enabled: Boolean,
    visible: Boolean,
    onRequestConfirmation: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    title: String = "خروج از صفحه؟",
    message: String = "تغییرات ذخیره‌نشده از بین می‌رود.",
) {
    BackHandler(enabled = enabled && !visible, onBack = onRequestConfirmation)
    FzDialog(visible, title, "خروج", onConfirm, onDismiss, message = message, dismissLabel = "ادامه ویرایش")
}
