package Ir.co.tfs.farazaman.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.data.db.DataHelper
import Ir.co.tfs.farazaman.databinding.ActivityActionsNotificationsBinding
import Ir.co.tfs.farazaman.supervisor.actions.UserActionMetadataKeys
import Ir.co.tfs.farazaman.supervisor.actions.UserActionOperations
import Ir.co.tfs.farazaman.supervisor.actions.UserActionRecord
import java.io.File
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ActionsNotificationsActivity : AppCompatActivity(), UserActionOperationHost {
    private lateinit var binding: ActivityActionsNotificationsBinding
    private var pendingGpxFile: File? = null
    private val saveGpxLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument(DataHelper.MIME_TYPE_GPX),
    ) { destination ->
        val source = pendingGpxFile
        pendingGpxFile = null
        if (destination == null || source == null) return@registerForActivityResult
        lifecycleScope.launch {
            val saved = withContext(Dispatchers.IO) {
                runCatching {
                    contentResolver.openOutputStream(destination, "w")?.use { output ->
                        source.inputStream().use { input -> input.copyTo(output) }
                    } ?: error("Unable to open GPX destination")
                }.isSuccess
            }
            Toast.makeText(
                this@ActionsNotificationsActivity,
                if (saved) R.string.action_gpx_saved else R.string.action_gpx_open_failed,
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActionsNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.actionsPager.adapter = ActionsPagerAdapter(this)
        TabLayoutMediator(binding.actionsTabs, binding.actionsPager) { tab, position ->
            tab.text = getString(
                if (position == ACTIONS_TAB_INDEX) R.string.actions_tab_title
                else R.string.notifications_tab_title,
            )
        }.attach()
        binding.actionsPager.setCurrentItem(ACTIONS_TAB_INDEX, false)
        binding.actionsTabs.post(::styleTabs)
        binding.actionsTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = styleTabs()
            override fun onTabUnselected(tab: TabLayout.Tab) = styleTabs()
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })
    }

    override fun onActionOperation(item: UserActionRecord, operationKey: String) {
        when (operationKey) {
            UserActionOperations.RESUME_MISSION -> requestMissionResume(item)
            UserActionOperations.OPEN_GPX -> openGpx(item)
        }
    }

    private fun requestMissionResume(item: UserActionRecord) {
        val trackId = item.relatedEntityId?.toLongOrNull() ?: return
        setResult(
            Activity.RESULT_OK,
            Intent().putExtra(EXTRA_RESUME_TRACK_ID, trackId),
        )
        finish()
    }

    private fun openGpx(item: UserActionRecord) {
        val path = item.metadata[UserActionMetadataKeys.GPX_PATH]
        val file = path?.let(::File)
        if (file == null || !file.isFile || file.length() == 0L) {
            Toast.makeText(this, R.string.action_gpx_missing, Toast.LENGTH_SHORT).show()
            return
        }
        pendingGpxFile = file
        saveGpxLauncher.launch(file.name)
    }

    private fun styleTabs() {
        val selectedBg = ContextCompat.getDrawable(this, R.drawable.supervisor_tab_selected)
        val unselectedBg = ContextCompat.getDrawable(this, R.drawable.supervisor_tab_unselected)
        val selectedColor = getColor(R.color.app_on_surface)
        val unselectedColor = getColor(R.color.app_on_surface_secondary)
        val padH = resources.getDimensionPixelSize(R.dimen.supervisor_tab_padding_h)
        val padV = resources.getDimensionPixelSize(R.dimen.supervisor_tab_padding_v)
        val halfGap = resources.getDimensionPixelSize(R.dimen.supervisor_tab_spacing) / 2
        val typeface = ResourcesCompat.getFont(this, R.font.iran_sans)

        for (index in 0 until binding.actionsTabs.tabCount) {
            val tab = binding.actionsTabs.getTabAt(index) ?: continue
            val selected = tab.position == binding.actionsTabs.selectedTabPosition
            tab.view.apply {
                setPadding(padH, padV, padH, padV)
                background = if (selected) selectedBg else unselectedBg
                minimumWidth = 0
                (layoutParams as? LinearLayout.LayoutParams)?.let { params ->
                    params.width = 0
                    params.weight = 1f
                    params.marginStart = halfGap
                    params.marginEnd = halfGap
                    layoutParams = params
                }
                findTabTextView(this)?.apply {
                    setTextColor(if (selected) selectedColor else unselectedColor)
                    textSize = 12f
                    includeFontPadding = false
                    setTypeface(
                        typeface,
                        if (selected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL,
                    )
                }
            }
        }
    }

    private fun findTabTextView(view: View): TextView? {
        if (view is TextView) return view
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                findTabTextView(view.getChildAt(index))?.let { return it }
            }
        }
        return null
    }

    private class ActionsPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment =
            if (position == ACTIONS_TAB_INDEX) UserActionsFragment() else FirebaseNotificationsFragment()
    }

    companion object {
        const val EXTRA_RESUME_TRACK_ID = "resume_track_id"
        private const val ACTIONS_TAB_INDEX = 0
    }
}

interface UserActionOperationHost {
    fun onActionOperation(item: UserActionRecord, operationKey: String)
}
