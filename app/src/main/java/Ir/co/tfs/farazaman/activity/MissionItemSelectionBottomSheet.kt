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
import Ir.co.tfs.farazaman.presentation.compose.mission.MissionItemSelectionSheetContent

class MissionItemSelectionBottomSheet : BottomSheetDialogFragment() {

    private var sheetTitle: String = ""
    private var items: List<MissionSelectionOption> = emptyList()
    var onItemSelected: ((MissionSelectionOption) -> Unit)? = null

    companion object {
        private const val SHEET_HEIGHT_RATIO = 0.75f

        fun newInstance(
            title: String,
            items: List<MissionSelectionOption>,
        ): MissionItemSelectionBottomSheet {
            return MissionItemSelectionBottomSheet().apply {
                sheetTitle = title
                this.items = items
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
                MissionItemSelectionSheetContent(
                    title = sheetTitle,
                    options = items,
                    onSelect = { item ->
                        onItemSelected?.invoke(item)
                        dismiss()
                    },
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog ?: return
        val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?: return
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)
        bottomSheet.layoutParams = bottomSheet.layoutParams.apply {
            height = (resources.displayMetrics.heightPixels * SHEET_HEIGHT_RATIO).toInt()
        }
        BottomSheetBehavior.from(bottomSheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
    }

}
