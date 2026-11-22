package Ir.co.tfs.farazaman.util

import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzButtonVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzLoading
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Window
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.activity.ComponentActivity
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

class LoadingDialog(context: Context) : Dialog(context) {
    private val messageState = mutableStateOf("در حال بارگذاری...")
    private val cancellableState = mutableStateOf(false)
    private var timeoutHandler: Handler? = null
    private var timeoutRunnable: Runnable? = null

    var onCancelListener: (() -> Unit)? = null
        set(value) {
            field = value
            cancellableState.value = value != null
            setCancelable(value != null)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCanceledOnTouchOutside(false)
        setCancelable(cancellableState.value)
        setOnCancelListener { onCancelListener?.invoke() }

        val composeView = ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            context.findComponentActivity()?.let { owner ->
                setViewTreeLifecycleOwner(owner)
                setViewTreeViewModelStoreOwner(owner)
                setViewTreeSavedStateRegistryOwner(owner)
            }
            setContent {
                FaraZamanTheme {
                    LoadingDialogContent(
                        message = messageState.value,
                        cancellable = cancellableState.value,
                        onCancel = {
                            onCancelListener?.invoke()
                            dismiss()
                        },
                    )
                }
            }
        }
        setContentView(composeView)
    }

    fun setLoadingText(text: String) {
        messageState.value = text
    }

    fun setTimeout(timeoutMs: Long) {
        cancelTimeout()
        timeoutHandler = Handler(Looper.getMainLooper())
        timeoutRunnable = Runnable { if (isShowing) dismiss() }
        timeoutHandler?.postDelayed(timeoutRunnable!!, timeoutMs)
    }

    fun cancelTimeout() {
        timeoutRunnable?.let { timeoutHandler?.removeCallbacks(it) }
        timeoutHandler = null
        timeoutRunnable = null
    }

    override fun dismiss() {
        cancelTimeout()
        super.dismiss()
    }

    companion object {
        fun show(
            context: Context,
            message: String = "در حال بارگذاری...",
            onCancel: (() -> Unit)? = null,
        ) = LoadingDialog(context).apply {
            setLoadingText(message)
            onCancelListener = onCancel
            show()
        }

        fun showWithTimeout(
            context: Context,
            message: String = "در حال بارگذاری...",
            timeoutMs: Long = 180000,
            onCancel: (() -> Unit)? = null,
        ) = show(context, message, onCancel).apply { setTimeout(timeoutMs) }
    }
}

private tailrec fun Context.findComponentActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findComponentActivity()
    else -> null
}

@Composable
private fun LoadingDialogContent(
    message: String,
    cancellable: Boolean,
    onCancel: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = FaraZamanThemeTokens.elevation.raised,
    ) {
        Column(Modifier.padding(FaraZamanThemeTokens.spacing.md)) {
            FzLoading(message)
            if (cancellable) {
                FzButton(
                    text = "انصراف",
                    onClick = onCancel,
                    variant = FzButtonVariant.Ghost,
                )
            }
        }
    }
}
