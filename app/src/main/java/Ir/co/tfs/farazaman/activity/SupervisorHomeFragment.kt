package Ir.co.tfs.farazaman.activity



import android.annotation.SuppressLint

import android.os.Bundle

import android.os.Handler

import android.os.Looper

import android.view.LayoutInflater

import android.view.MotionEvent

import android.view.View

import android.view.ViewGroup

import android.widget.TextView

import android.widget.Toast

import androidx.fragment.app.Fragment

import androidx.lifecycle.lifecycleScope

import com.google.android.material.dialog.MaterialAlertDialogBuilder

import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.R.string.supervisor_tracking_started

import Ir.co.tfs.farazaman.databinding.FragmentSupervisorHomeBinding

import Ir.co.tfs.farazaman.supervisor.SupervisorMissionHelper

import Ir.co.tfs.farazaman.supervisor.OrganFenceContainment

import Ir.co.tfs.farazaman.supervisor.SupervisorVisitTab

import Ir.co.tfs.farazaman.supervisor.SupervisorViolationHistoryStore
import Ir.co.tfs.farazaman.notification.NotificationBadgeBinder

import Ir.co.tfs.farazaman.supervisor.bindSupervisorProfileButton

import saman.zamani.persiandate.PersianDate

import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch

import kotlinx.coroutines.withContext



class SupervisorHomeFragment : Fragment() {



    private var _binding: FragmentSupervisorHomeBinding? = null

    private val binding get() = _binding!!

    private val holdHandler = Handler(Looper.getMainLooper())

    private var holdRunnable: Runnable? = null

    private var holdStartMs = 0L

    private var holdCompleted = false

    private var wasTracking = false
    private var lastKnownTracking: Boolean? = null

    private val fenceStatusRunnable = object : Runnable {
        override fun run() {
            refreshFenceStatus()
            holdHandler.postDelayed(this, FENCE_STATUS_REFRESH_MS)
        }
    }



    companion object {

        private const val HOLD_END_MS = 3000L

        private const val TAP_MAX_MS = 350L

        private const val FENCE_STATUS_REFRESH_MS = 3_000L

    }



    override fun onCreateView(

        inflater: LayoutInflater,

        container: ViewGroup?,

        savedInstanceState: Bundle?,

    ): View {

        _binding = FragmentSupervisorHomeBinding.inflate(inflater, container, false)

        return binding.root

    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        binding.btnNotifications.setOnClickListener { hostActivity()?.openActionsNotifications() }
        NotificationBadgeBinder.bind(binding.btnNotifications, viewLifecycleOwner)

        binding.root.bindSupervisorProfileButton()

        binding.btnChangeContract.setOnClickListener { confirmChangeContract() }

        binding.btnShowOnMap.setOnClickListener {
            hostActivity()?.controller?.openMissionMap()
        }

        setupStatCards()

        setupMissionButton()

        refreshUi()

        holdHandler.post(fenceStatusRunnable)

    }



    private fun setupStatCards() {

        bindStatCard(binding.statDailyTasks.root, getString(R.string.supervisor_home_stat_daily)) {
            hostActivity()?.navigateToTodayTab(SupervisorVisitTab.PLANNING)
        }
        bindStatCard(binding.statContinuousTasks.root, getString(R.string.supervisor_home_stat_continuous)) {
            hostActivity()?.navigateToTodayTab(SupervisorVisitTab.CONTINUOUS)
        }
        bindStatCard(binding.statViolations.root, getString(R.string.supervisor_home_stat_violations)) {
            hostActivity()?.navigateToTodayTab(SupervisorVisitTab.GENERAL)
        }

    }



    private fun bindStatCard(root: View, title: String, onClick: () -> Unit) {

        root.findViewById<TextView>(R.id.statTitle).text = title

        root.setOnClickListener { onClick() }

    }



    private fun confirmChangeContract() {

        val controller = hostActivity()?.controller ?: return

        if (controller.isTracking()) {

            Toast.makeText(requireContext(), R.string.supervisor_change_contract_blocked, Toast.LENGTH_SHORT).show()

            return

        }

        MaterialAlertDialogBuilder(requireContext())

            .setMessage(R.string.supervisor_change_contract_message)

            .setPositiveButton(R.string.supervisor_change_contract_confirm) { _, _ ->

                controller.deleteMission()

                SupervisorMissionHelper.openWorkAreaSelection(requireContext())

                requireActivity().finish()

            }

            .setNegativeButton(R.string.supervisor_change_contract_cancel, null)

            .show()

    }



    override fun onResume() {

        super.onResume()

        val activity = hostActivity() ?: return

        activity.controller.loadState()

        if (!activity.controller.hasMission()) {

            SupervisorMissionHelper.openWorkAreaSelection(requireContext())

            activity.finish()

            return

        }

        refreshUi()

    }



    override fun onDestroyView() {

        holdRunnable?.let { holdHandler.removeCallbacks(it) }

        holdHandler.removeCallbacks(fenceStatusRunnable)

        _binding = null

        super.onDestroyView()

    }



    fun refreshUi() {

        val activity = hostActivity() ?: return

        val controller = activity.controller

        if (!controller.hasMission()) return



        val tracking = controller.isTracking()
        if (lastKnownTracking != null && tracking && lastKnownTracking == false) {
            activity.showSnackbarAboveNavigation(supervisor_tracking_started)
        }
        lastKnownTracking = tracking
        wasTracking = tracking



        binding.warningBanner.visibility = if (tracking) View.GONE else View.VISIBLE

        binding.activeBanner.visibility = if (tracking) View.VISIBLE else View.GONE

        if (!tracking) binding.fenceStatusText.visibility = View.GONE

        binding.missionHint.visibility = if (tracking) View.VISIBLE else View.GONE



        binding.visitStatusTitle.text = if (tracking) {

            getString(R.string.supervisor_home_visit_in_progress)

        } else {

            getString(R.string.supervisor_home_visit_not_started)

        }



        when {

            tracking -> {

                binding.btnMission.text = getString(R.string.supervisor_stop)

                binding.btnMission.setBackgroundResource(R.drawable.supervisor_home_stop_btn)

                binding.btnMission.setTextColor(requireContext().getColor(R.color.app_on_error))

            }

            else -> {

                binding.btnMission.text = getString(R.string.supervisor_start_visit)

                binding.btnMission.setBackgroundResource(R.drawable.supervisor_home_start_btn)

                binding.btnMission.setTextColor(requireContext().getColor(R.color.app_on_primary_container))

            }

        }



        if (!holdRunnableRunning()) {

            binding.missionHoldProgress.visibility = View.GONE

            binding.missionHoldProgress.progress = 0

        }



        val displayName = activity.userManager.getDisplayName()?.takeIf { it.isNotBlank() }

            ?: activity.userManager.getUserName()

            ?: getString(R.string.role_supervisor)

        binding.greetingText.text = getString(R.string.supervisor_home_greeting, displayName)

        binding.dateText.text = formatTodayPersian()



        val info = controller.missionInfo(controller.currentTrackId)

        if (info != null) {

            binding.contractInfoText.text = getString(
                R.string.supervisor_home_contract_detail,
                info.organTitle,
                info.contractTitle,
            )

            val violationCount = SupervisorViolationHistoryStore.loadAll(requireContext())

                .count { it.trackId == controller.currentTrackId }

            updateStatCount(binding.statDailyTasks.root, info.planningCount)

            updateStatCount(binding.statContinuousTasks.root, info.systemCount)

            updateStatCount(binding.statViolations.root, violationCount)

        }

    }



    private fun updateStatCount(root: View, count: Int) {

        root.findViewById<TextView>(R.id.statCount).text =

            getString(R.string.supervisor_home_stat_count, count)

    }

    private fun refreshFenceStatus() {
        val activity = hostActivity() ?: return
        val controller = activity.controller
        if (!controller.isTracking()) {
            _binding?.fenceStatusText?.visibility = View.GONE
            return
        }
        val trackId = controller.currentTrackId
        viewLifecycleOwner.lifecycleScope.launch {
            val containment = withContext(Dispatchers.IO) {
                val fenceWkt = controller.missionInfo(trackId)?.organFenceWkt.orEmpty()
                val location = controller.lastTrackLocation()
                if (fenceWkt.isBlank() || location == null) null else {
                    OrganFenceContainment.contains(
                        wkt = fenceWkt,
                        latitude = location.latitude,
                        longitude = location.longitude,
                    )
                }
            }
            val currentBinding = _binding ?: return@launch
            if (!controller.isTracking() || controller.currentTrackId != trackId || containment == null) {
                currentBinding.fenceStatusText.visibility = View.GONE
                return@launch
            }
            currentBinding.fenceStatusText.setText(
                if (containment) R.string.supervisor_home_inside_fence
                else R.string.supervisor_home_outside_fence,
            )
            currentBinding.fenceStatusText.setTextColor(
                currentBinding.root.context.getColor(
                    if (containment) R.color.intro_primary else R.color.app_error,
                ),
            )
            currentBinding.fenceStatusText.visibility = View.VISIBLE
        }
    }



    private fun formatTodayPersian(): String {

        val pd = PersianDate()

        return "${persianWeekday(pd)}، ${pd.shDay} ${persianMonthName(pd.shMonth)}"

    }



    private fun persianWeekday(date: PersianDate): String = when (date.dayOfWeek()) {

        0 -> "شنبه"

        1 -> "یکشنبه"

        2 -> "دوشنبه"

        3 -> "سه‌شنبه"

        4 -> "چهارشنبه"

        5 -> "پنج‌شنبه"

        else -> "جمعه"

    }



    private fun persianMonthName(month: Int): String = when (month) {

        1 -> "فروردین"

        2 -> "اردیبهشت"

        3 -> "خرداد"

        4 -> "تیر"

        5 -> "مرداد"

        6 -> "شهریور"

        7 -> "مهر"

        8 -> "آبان"

        9 -> "آذر"

        10 -> "دی"

        11 -> "بهمن"

        else -> "اسفند"

    }



    private fun holdRunnableRunning(): Boolean = holdRunnable != null



    @SuppressLint("ClickableViewAccessibility")

    private fun setupMissionButton() {

        binding.missionButtonContainer.setOnTouchListener { _, event ->

            val controller = hostActivity()?.controller ?: return@setOnTouchListener false

            if (!controller.hasMission()) return@setOnTouchListener false



            when (event.action) {

                MotionEvent.ACTION_DOWN -> {

                    holdCompleted = false

                    holdStartMs = System.currentTimeMillis()

                    binding.missionHoldProgress.progress = 0

                    holdRunnable = object : Runnable {

                        override fun run() {

                            val elapsed = System.currentTimeMillis() - holdStartMs

                            if (elapsed > TAP_MAX_MS) {

                                binding.missionHoldProgress.visibility = View.VISIBLE

                            }

                            binding.missionHoldProgress.progress =

                                elapsed.toInt().coerceAtMost(HOLD_END_MS.toInt())

                            if (elapsed >= HOLD_END_MS) {

                                holdCompleted = true

                                cancelHoldProgress()

                                controller.confirmEndMission()

                            } else {

                                holdHandler.postDelayed(this, 40)

                            }

                        }

                    }

                    holdHandler.post(holdRunnable!!)

                    true

                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

                    cancelHoldProgress()

                    if (holdCompleted) return@setOnTouchListener true



                    val elapsed = System.currentTimeMillis() - holdStartMs

                    when {

                        elapsed < TAP_MAX_MS && !controller.isTracking() -> controller.startMission()

                        elapsed < TAP_MAX_MS && controller.isTracking() -> controller.stopMission()

                    }

                    refreshUi()

                    true

                }

                else -> false

            }

        }

    }



    private fun cancelHoldProgress() {

        holdRunnable?.let { holdHandler.removeCallbacks(it) }

        holdRunnable = null

        binding.missionHoldProgress.visibility = View.GONE

        binding.missionHoldProgress.progress = 0

    }



    private fun hostActivity(): NewSupervisorActivity? = activity as? NewSupervisorActivity

}

