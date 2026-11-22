package Ir.co.tfs.farazaman.activity

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import Ir.co.tfs.farazaman.presentation.compose.FaraZamanTheme
import Ir.co.tfs.farazaman.presentation.compose.mission.EndMissionSheetContent

class EndMissionBottomSheet : BottomSheetDialogFragment() {

    var onConfirm: (() -> Unit)? = null

    companion object {
        fun newInstance(onConfirm: () -> Unit): EndMissionBottomSheet {
            return EndMissionBottomSheet().apply {
                this.onConfirm = onConfirm
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            FaraZamanTheme {
                EndMissionSheetContent(
                    onConfirm = {
                        dismiss()
                        onConfirm?.invoke()
                    },
                    onCancel = ::dismiss,
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val sheet = (dialog as? BottomSheetDialog)
            ?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?: return
        sheet.setBackgroundColor(Color.TRANSPARENT)
        BottomSheetBehavior.from(sheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
    }
}
