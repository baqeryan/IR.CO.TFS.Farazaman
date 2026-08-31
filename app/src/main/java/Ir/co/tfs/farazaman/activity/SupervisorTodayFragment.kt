package Ir.co.tfs.farazaman.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.databinding.FragmentSupervisorTodayBinding
import Ir.co.tfs.farazaman.supervisor.bindSupervisorProfileButton
import Ir.co.tfs.farazaman.data.repository.AddressLookupCoordinator
import Ir.co.tfs.farazaman.domain.road.RoadWidthCategory
import Ir.co.tfs.farazaman.util.AddressLocationHelper
import Ir.co.tfs.farazaman.util.RoadQueryBuffer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SupervisorTodayFragment : Fragment() {

    @Inject lateinit var addressLookup: AddressLookupCoordinator

    private var _binding: FragmentSupervisorTodayBinding? = null
    private val binding get() = _binding!!
    private var streetLookupJob: Job? = null
    private var streetLookupTrackId: Long = Ir.co.tfs.farazaman.supervisor.SupervisorTrackController.TRACK_ID_NONE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSupervisorTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.bindSupervisorProfileButton()
        binding.btnActiveVisitShowOnMap.setOnClickListener {
            (activity as? NewSupervisorActivity)?.controller?.openMissionMap()
        }
        val tabTitles = listOf(
            getString(R.string.supervisor_tab_planning),
            getString(R.string.supervisor_tab_continuous),
            getString(R.string.supervisor_tab_commitments),
        )
        binding.visitPager.adapter = VisitPagerAdapter(this)
        TabLayoutMediator(binding.visitTabs, binding.visitPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
        binding.visitTabs.post { styleVisitTabs() }
        binding.visitTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = styleVisitTabs()
            override fun onTabUnselected(tab: TabLayout.Tab) = styleVisitTabs()
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })
    }

    override fun onResume() {
        super.onResume()
        (activity as? NewSupervisorActivity)?.syncTrackIdToIntent()
        refreshContent()
    }

    fun refreshContent() {
        refreshHeaderState()
        childFragmentManager.fragments.forEach { fragment ->
            (fragment as? TodayVisitTabFragment)?.reloadContent()
        }
    }

    private fun refreshHeaderState() {
        val host = activity as? NewSupervisorActivity
        val tracking = host?.controller?.isTracking() == true
        binding.activeVisitBanner.visibility = if (tracking) View.VISIBLE else View.GONE
        if (!tracking || host == null) {
            streetLookupJob?.cancel()
            streetLookupJob = null
            streetLookupTrackId = Ir.co.tfs.farazaman.supervisor.SupervisorTrackController.TRACK_ID_NONE
            binding.currentStreetName.visibility = View.GONE
            binding.currentStreetName.text = ""
            return
        }

        resolveCurrentStreet(host)
    }

    private fun resolveCurrentStreet(host: NewSupervisorActivity) {
        val trackId = host.controller.currentTrackId
        if (trackId == Ir.co.tfs.farazaman.supervisor.SupervisorTrackController.TRACK_ID_NONE) return
        if (streetLookupTrackId == trackId && streetLookupJob?.isActive == true) return

        streetLookupJob?.cancel()
        if (streetLookupTrackId != trackId) {
            binding.currentStreetName.text = ""
            binding.currentStreetName.visibility = View.GONE
        }
        streetLookupTrackId = trackId
        streetLookupJob = viewLifecycleOwner.lifecycleScope.launch {
            var lastResolvedCoordinates: Pair<Double, Double>? = null
            while (isActive && host.controller.isTracking() && host.controller.currentTrackId == trackId) {
                val point = withContext(Dispatchers.IO) { host.controller.lastTrackLocation() }
                val coordinates = point?.let { it.latitude to it.longitude }
                if (coordinates != null &&
                    AddressLocationHelper.shouldUpdateAddress(lastResolvedCoordinates, coordinates) &&
                    !addressLookup.isCoolingDown()
                ) {
                    try {
                        val roadData = addressLookup.getRoadData(
                            latitude = coordinates.first,
                            longitude = coordinates.second,
                            buffer = RoadQueryBuffer.BUFFER_DEFAULT,
                        ).firstOrNull()
                        val streetName = roadData?.name?.trim().orEmpty()
                        val streetSummary = if (roadData != null && streetName.isNotEmpty()) {
                            getString(
                                R.string.map_road_name_with_width,
                                streetName,
                                getString(RoadWidthCategory.fromFclass(roadData.fclass).labelResource()),
                            )
                        } else {
                            streetName
                        }

                        lastResolvedCoordinates = coordinates
                        if (_binding != null && streetSummary.isNotEmpty() && host.controller.isTracking()) {
                            binding.currentStreetName.text = streetSummary
                            binding.currentStreetName.visibility = View.VISIBLE
                        }
                    } catch (error: Exception) {
                        Log.w(TAG, "Unable to resolve current street", error)
                    }
                }

                delay(STREET_LOCATION_POLL_INTERVAL_MS)
            }
        }
    }

    fun selectVisitTab(index: Int): Boolean {
        val binding = _binding ?: return false
        if (index !in 0 until binding.visitPager.adapter?.itemCount.orZero()) return false
        binding.visitPager.setCurrentItem(index, true)
        binding.visitTabs.getTabAt(index)?.select()
        styleVisitTabs()
        return true
    }

    private fun Int?.orZero(): Int = this ?: 0

    private fun RoadWidthCategory.labelResource(): Int = when (this) {
        RoadWidthCategory.UP_TO_15_METERS -> R.string.road_width_up_to_15
        RoadWidthCategory.OVER_15_TO_25_METERS -> R.string.road_width_over_15_to_25
        RoadWidthCategory.OVER_25_TO_45_METERS -> R.string.road_width_over_25_to_45
        RoadWidthCategory.OVER_45_METERS -> R.string.road_width_over_45
    }

    override fun onDestroyView() {
        streetLookupJob?.cancel()
        streetLookupJob = null
        _binding = null
        super.onDestroyView()
    }

    private fun styleVisitTabs() {
        val selectedBg = ContextCompat.getDrawable(requireContext(), R.drawable.supervisor_tab_selected)
        val unselectedBg = ContextCompat.getDrawable(requireContext(), R.drawable.supervisor_tab_unselected)
        val selectedColor = requireContext().getColor(R.color.login_label)
        val unselectedColor = requireContext().getColor(R.color.text_secondary_dark)
        val padH = resources.getDimensionPixelSize(R.dimen.supervisor_tab_padding_h)
        val padV = resources.getDimensionPixelSize(R.dimen.supervisor_tab_padding_v)
        val tabGap = resources.getDimensionPixelSize(R.dimen.supervisor_tab_spacing)
        val halfGap = tabGap / 2
        val tabTypeface = ResourcesCompat.getFont(requireContext(), R.font.iran_sans)

        for (i in 0 until binding.visitTabs.tabCount) {
            val tab = binding.visitTabs.getTabAt(i) ?: continue
            val selected = tab.position == binding.visitTabs.selectedTabPosition
            val tabView = tab.view
            tabView.setPadding(padH, padV, padH, padV)
            tabView.background = if (selected) selectedBg else unselectedBg
            tabView.minimumWidth = 0
            (tabView.layoutParams as? LinearLayout.LayoutParams)?.let { lp ->
                lp.width = 0
                lp.weight = 1f
                lp.marginStart = halfGap
                lp.marginEnd = halfGap
                tabView.layoutParams = lp
            }
            findTabTextView(tabView)?.apply {
                setTextColor(if (selected) selectedColor else unselectedColor)
                textSize = 12f
                includeFontPadding = false
                setTypeface(
                    tabTypeface,
                    if (selected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL,
                )
            }
        }
    }

    private fun findTabTextView(view: View): TextView? {
        if (view is TextView) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                findTabTextView(view.getChildAt(i))?.let { return it }
            }
        }
        return null
    }

    private class VisitPagerAdapter(fragment: SupervisorTodayFragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 3
        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> TodayVisitTabFragment.newInstance("planning")
            1 -> TodayVisitTabFragment.newInstance("system")
            else -> TodayVisitTabFragment.newInstance("commitments")
        }
    }

    companion object {
        private const val TAG = "SupervisorToday"
        private const val STREET_LOCATION_POLL_INTERVAL_MS = 1_000L
    }
}
