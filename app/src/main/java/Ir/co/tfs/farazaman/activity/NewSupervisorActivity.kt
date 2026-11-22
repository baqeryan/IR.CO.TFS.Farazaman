package Ir.co.tfs.farazaman.activity

import android.app.Activity
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.data.api.FormDataApiService
import Ir.co.tfs.farazaman.data.db.model.TrackPoint
import Ir.co.tfs.farazaman.data.model.DropdownItem
import Ir.co.tfs.farazaman.data.model.FormDataRequest
import Ir.co.tfs.farazaman.data.model.FormDataResponse
import Ir.co.tfs.farazaman.data.repository.AddressLookupCoordinator
import Ir.co.tfs.farazaman.data.db.TrackContentProvider
import Ir.co.tfs.farazaman.domain.road.RoadWidthCategory
import Ir.co.tfs.farazaman.notification.NotificationUnreadCountStore
import Ir.co.tfs.farazaman.presentation.compose.*
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import Ir.co.tfs.farazaman.presentation.viewmodel.FormDataViewModel
import Ir.co.tfs.farazaman.supervisor.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import saman.zamani.persiandate.PersianDate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import Ir.co.tfs.farazaman.util.RoadQueryBuffer
import Ir.co.tfs.farazaman.util.AddressLocationHelper
import Ir.co.tfs.farazaman.util.BackgroundLocationPermissionHelper
import Ir.co.tfs.farazaman.util.BillTenantContextStore
import Ir.co.tfs.farazaman.map.BaranMapStyle
import Ir.co.tfs.farazaman.map.ComposeMapHost
import Ir.co.tfs.farazaman.map.MapRoadNameResolver
import javax.inject.Inject
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

@AndroidEntryPoint
class NewSupervisorActivity : AppCompatActivity(), SupervisorMissionHost {
    @Inject lateinit var okHttpClient: OkHttpClient
    @Inject lateinit var userManager: Ir.co.tfs.farazaman.util.UserManager
    @Inject lateinit var formDataApiService: FormDataApiService
    @Inject lateinit var addressLookup: AddressLookupCoordinator
    @Inject lateinit var billTenantContextStore: BillTenantContextStore
    private val commitmentViewModel: FormDataViewModel by viewModels()
    lateinit var controller: SupervisorTrackController; private set

    private var tab by mutableStateOf(SupervisorShellTab.Home)
    private var home by mutableStateOf(SupervisorHomeUiState())
    private var today by mutableStateOf(SupervisorTodayUiState())
    private var history by mutableStateOf(SupervisorHistoryUiState())
    private var planning = emptyList<DailyPlanItem>()
    private var continuous = emptyList<DailyPlanItem>()
    private val dates = mutableListOf<PersianDate>()
    private var selectedDate = PersianDate()
    private var dayJob: Job? = null
    private var watcherJob: Job? = null
    private var todayRoadWatcherJob: Job? = null
    private var todayRoadJob: Job? = null
    private var lastTodayRoadCoordinates: Pair<Double, Double>? = null
    private var backgroundRoadMapView: MapView? = null
    private var backgroundRoadMap: MapLibreMap? = null
    private var pendingBackgroundRoadLocation: Pair<Double, Double>? = null
    private var backgroundRoadRequestToken = 0L
    private val pendingActions = mutableListOf<() -> Unit>()
    private var commitmentData: FormDataResponse? = null
    private var commitmentGroup: DropdownItem? = null
    private var commitmentViolation: DropdownItem? = null
    private var commitmentItemGroup: DropdownItem? = null
    private var commitmentOriginItem: DropdownItem? = null
    private var commitmentViolationsRequestedForGroupId: Int? = null
    private val commitmentImages = mutableListOf<Uri>()
    private lateinit var commitmentCamera: ViolationImageCaptureCoordinator
    private enum class CommitmentSelector { Group, Violation, ItemGroup, OriginItem }
    private var commitmentSelectorOrder = listOf(
        CommitmentSelector.Group,
        CommitmentSelector.Violation,
        CommitmentSelector.ItemGroup,
        CommitmentSelector.OriginItem,
    )

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.isNotEmpty() && result.values.all { it }) commitmentCamera.captureNew()
        else Toast.makeText(this, "برای استفاده از دوربین، دسترسی لازم است.", Toast.LENGTH_SHORT).show()
    }

    private val actionsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val id = result.data?.getLongExtra(ActionsNotificationsActivity.EXTRA_RESUME_TRACK_ID, -1L) ?: -1L
            controller.loadState()
            if (id == controller.currentTrackId && controller.hasMission() && !controller.isTracking()) controller.startMission()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!SupervisorMissionHelper.hasExistingMission(this)) { SupervisorMissionHelper.openWorkAreaSelection(this); finish(); return }
        MapLibre.getInstance(this)
        controller = SupervisorTrackController(this, okHttpClient, userManager, ::onMissionUpdated)
        setupCommitmentForm()
        buildDates()
        setContent { FaraZamanTheme {
            Box(Modifier.fillMaxSize()) {
                ComposeMapHost(
                    modifier = Modifier.fillMaxSize().alpha(0.01f),
                    keepScreenOn = false,
                    onMapViewReady = ::initializeBackgroundRoadMap,
                )
                SupervisorScaffold(tab, ::selectTab) { scaffoldPadding ->
                    val bottomContentPadding = scaffoldPadding.calculateBottomPadding()
                    SupervisorSwipePager(tab, ::selectTab) { page ->
                        when (page) {
                            SupervisorShellTab.Home -> SupervisorHomeScreen(home, ::homeEvent, bottomContentPadding = bottomContentPadding)
                            SupervisorShellTab.Today -> SupervisorTodayScreen(today, ::todayEvent, bottomContentPadding = bottomContentPadding)
                            SupervisorShellTab.History -> SupervisorHistoryScreen(history, ::historyEvent, bottomContentPadding = bottomContentPadding)
                        }
                    }
                }
            }
        } }
    }

    override fun onResume() { super.onResume(); controller.loadState(); syncTrackIdToIntent(); refreshAll(); ensureCurrentDayMission(); startWatcher(); startTodayRoadWatcher() }
    override fun onPause() { watcherJob?.cancel(); watcherJob = null; todayRoadWatcherJob?.cancel(); todayRoadWatcherJob = null; todayRoadJob?.cancel(); todayRoadJob = null; super.onPause() }
    override fun onMissionUpdated() { controller.loadState(); syncTrackIdToIntent(); refreshAll() }
    override fun onRequestPermissionsResult(code: Int, permissions: Array<out String>, results: IntArray) { super.onRequestPermissionsResult(code, permissions, results); controller.onRequestPermissionsResult(code, results); refreshAll() }

    private fun refreshAll() { refreshHome(); refreshToday(); SupervisorViolationHistoryStore.syncStatusesFromMissionData(this); refreshHistory() }
    private fun refreshHome() {
        val info = controller.missionInfo(controller.currentTrackId)
        val name = userManager.getDisplayName()?.takeIf { it.isNotBlank() } ?: userManager.getUserName() ?: getString(R.string.role_supervisor)
        val date = PersianDate(); val weekdays = arrayOf("شنبه","یکشنبه","دوشنبه","سه‌شنبه","چهارشنبه","پنج‌شنبه","جمعه")
        home = home.copy(
            greeting = getString(R.string.supervisor_home_greeting, name), date = "${weekdays[date.dayOfWeek().coerceIn(0,6)]}، ${date.shYear}/${date.shMonth}/${date.shDay}",
            missionTitle = info?.let { getString(R.string.supervisor_home_contract_detail, it.organTitle, it.contractTitle) }, tracking = controller.isTracking(),
            dailyTasks = info?.planningCount ?: 0, continuousTasks = info?.systemCount ?: 0,
            violations = SupervisorViolationHistoryStore.loadAll(this).count { it.trackId == controller.currentTrackId }, unreadNotifications = NotificationUnreadCountStore.read(this),
            backgroundLocationGranted = BackgroundLocationPermissionHelper.hasPermission(this),
            batteryOptimizationIgnored = BackgroundLocationPermissionHelper.isBatteryOptimizationIgnored(this),
        )
    }
    private fun homeEvent(e: SupervisorHomeEvent) { when(e) {
        SupervisorHomeEvent.StartVisit -> controller.startMission(); SupervisorHomeEvent.StopVisit -> controller.stopMission(); SupervisorHomeEvent.EndVisit -> controller.confirmEndMission()
        SupervisorHomeEvent.OpenMap -> controller.openMissionMap(); SupervisorHomeEvent.OpenNotifications -> openActionsNotifications(); SupervisorHomeEvent.OpenProfile -> SupervisorProfileActivity.open(this)
        SupervisorHomeEvent.ChangeWorkArea -> if (controller.isTracking()) Toast.makeText(this,R.string.supervisor_change_contract_blocked,Toast.LENGTH_SHORT).show() else { controller.deleteMission(); SupervisorMissionHelper.openWorkAreaSelection(this); finish() }
        SupervisorHomeEvent.OpenDailyTasks -> navigateToTodayTab(SupervisorVisitTab.PLANNING); SupervisorHomeEvent.OpenContinuousTasks -> navigateToTodayTab(SupervisorVisitTab.CONTINUOUS); SupervisorHomeEvent.OpenViolations -> navigateToTodayTab(SupervisorVisitTab.GENERAL)
        SupervisorHomeEvent.RequestBackgroundLocation -> BackgroundLocationPermissionHelper.requestPermission(this)
        SupervisorHomeEvent.OpenBatteryOptimizationSettings -> BackgroundLocationPermissionHelper.openBatteryOptimizationSettings(this)
    }; refreshAll() }

    private fun refreshToday() {
        val id=controller.currentTrackId; val counts=SupervisorViolationHistoryStore.countByItemTitle(this,id)
        planning=loadItems(id,"planning",counts); continuous=loadItems(id,"system",counts)
        today=today.copy(planningItems=planning.ui("p"),continuousItems=continuous.ui("c"),tracking=controller.isTracking(),unreadNotifications=NotificationUnreadCountStore.read(this))
        refreshTodayRoadInfo()
    }
    private fun refreshTodayRoadInfo() {
        val location = controller.lastTrackLocation() ?: return
        val coordinates = location.latitude to location.longitude
        if (!AddressLocationHelper.shouldUpdateAddress(lastTodayRoadCoordinates, coordinates) || todayRoadJob?.isActive == true || addressLookup.isCoolingDown()) return
        todayRoadJob = lifecycleScope.launch {
            try {
                val road = addressLookup.getRoadData(location.latitude, location.longitude, RoadQueryBuffer.BUFFER_DEFAULT).firstOrNull()
                if (road != null) {
                    lastTodayRoadCoordinates = coordinates
                    val streetName = road.name.takeIf { it.isNotBlank() }
                        ?: getString(R.string.map_address_unknown)
                    today = today.copy(
                        currentStreet = streetName,
                        currentRoadWidth = getString(RoadWidthCategory.fromFclass(road.fclass).labelResource()),
                        commitmentForm = today.commitmentForm.copy(address = streetName),
                    )
                } else {
                    resolveTodayRoadFromMap(location.latitude, location.longitude)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.w("Supervisor", "Today road lookup failed", error)
                resolveTodayRoadFromMap(location.latitude, location.longitude)
            } finally {
                todayRoadJob = null
            }
        }
    }

    private fun initializeBackgroundRoadMap(mapView: MapView) {
        backgroundRoadMapView = mapView
        mapView.getMapAsync { map ->
            lifecycleScope.launch {
                runCatching { BaranMapStyle.load(this@NewSupervisorActivity) }
                    .onSuccess { styleJson ->
                        map.setStyle(Style.Builder().fromJson(styleJson)) {
                            backgroundRoadMap = map
                            pendingBackgroundRoadLocation?.also { (latitude, longitude) ->
                                pendingBackgroundRoadLocation = null
                                resolveTodayRoadFromMap(latitude, longitude)
                            }
                        }
                    }
                    .onFailure { error -> Log.w("Supervisor", "Background road map failed", error) }
            }
        }
    }

    private fun resolveTodayRoadFromMap(latitude: Double, longitude: Double) {
        val map = backgroundRoadMap
        val mapView = backgroundRoadMapView
        if (map == null || mapView == null) {
            pendingBackgroundRoadLocation = latitude to longitude
            return
        }

        val token = ++backgroundRoadRequestToken
        val point = LatLng(latitude, longitude)
        map.cameraPosition = CameraPosition.Builder().target(point).zoom(17.0).build()

        fun attempt(remaining: Int) {
            mapView.postDelayed({
                if (token != backgroundRoadRequestToken || isFinishing || isDestroyed) return@postDelayed
                val road = MapRoadNameResolver.resolve(map, point)
                if (road != null) {
                    lastTodayRoadCoordinates = latitude to longitude
                    today = today.copy(
                        currentStreet = road.name,
                        currentRoadWidth = road.roadClass.takeIf(String::isNotBlank)
                            ?.let { getString(RoadWidthCategory.fromFclass(it).labelResource()) }
                            .orEmpty(),
                        commitmentForm = today.commitmentForm.copy(address = road.name),
                    )
                } else if (remaining > 1) {
                    attempt(remaining - 1)
                }
            }, BACKGROUND_MAP_RETRY_DELAY_MS)
        }

        attempt(BACKGROUND_MAP_RETRY_COUNT)
    }
    private fun RoadWidthCategory.labelResource(): Int = when (this) {
        RoadWidthCategory.UP_TO_15_METERS -> R.string.road_width_up_to_15
        RoadWidthCategory.OVER_15_TO_25_METERS -> R.string.road_width_over_15_to_25
        RoadWidthCategory.OVER_25_TO_45_METERS -> R.string.road_width_over_25_to_45
        RoadWidthCategory.OVER_45_METERS -> R.string.road_width_over_45
    }
    private fun todayEvent(e: SupervisorTodayEvent) { when(e) {
        is SupervisorTodayEvent.SelectTab -> {
            today = today.copy(selectedTab = e.tab)
            if (e.tab == SupervisorTodayTab.Commitments) loadCommitmentForm()
        }
        is SupervisorTodayEvent.OpenMap -> findItem(e.id)?.let { TodayVisitMissionActions.openMap(this,controller.currentTrackId,it.encryption) }
        is SupervisorTodayEvent.RegisterViolation -> findItem(e.id)?.let { openDailyPlanViolation(controller.currentTrackId,if(today.selectedTab==SupervisorTodayTab.Planning)"planning" else "system",it.title) }
        SupervisorTodayEvent.StartVisit -> controller.startMission(); SupervisorTodayEvent.OpenActiveVisitMap -> controller.openMissionMap()
        SupervisorTodayEvent.OpenHome -> selectTab(SupervisorShellTab.Home)
        SupervisorTodayEvent.OpenHistory -> selectTab(SupervisorShellTab.History)
        SupervisorTodayEvent.OpenNotifications -> openActionsNotifications(); SupervisorTodayEvent.OpenProfile -> SupervisorProfileActivity.open(this); SupervisorTodayEvent.Retry -> refreshToday()
        is SupervisorTodayEvent.CommitmentForm -> handleCommitmentFormEvent(e.event)
    }; refreshAll() }
    private fun findItem(id:String):DailyPlanItem? { val i=id.substringAfterLast(':').toIntOrNull()?:return null; return if(today.selectedTab==SupervisorTodayTab.Planning) planning.getOrNull(i) else continuous.getOrNull(i) }
    private fun List<DailyPlanItem>.ui(prefix: String) = mapIndexed { index, item ->
        SupervisorTaskUiItem(
            id = "$prefix:$index",
            title = item.title,
            subtitle = item.subtitle,
            violationCount = item.violationCount,
            canRegisterViolation = item.canRegisterViolation,
            billSystemCode = item.billSystemCode,
        )
    }

    private fun loadItems(id: Long, type: String, counts: Map<String, Int>): List<DailyPlanItem> {
        val prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(this)
        val json = prefs.getString("mission_data_$id", null) ?: return emptyList()
        return runCatching {
            val items = JSONObject(json).optJSONArray(
                if (type == "planning") "planningItems" else "systemItems",
            ) ?: JSONArray()
            buildList {
                for (index in 0 until items.length()) {
                    val item = items.getJSONObject(index)
                    val title = item.optString("billCleaningItemName").trim()
                    if (title.isEmpty()) continue
                    val storedCode = item.optString("bilsystemcode").trim()
                    val rawCode = item.optString("rawData").takeIf(String::isNotBlank)?.let { raw ->
                        runCatching { JSONObject(raw).extractBillSystemCode() }.getOrDefault("")
                    }.orEmpty()
                    add(
                        DailyPlanItem(
                            title = title,
                            subtitle = item.optString("description"),
                            encryption = item.optString("aencryption"),
                            violationCount = counts[title] ?: 0,
                            canRegisterViolation = DailyPlanItemViolationHelper.canRegisterViolation(item),
                            billSystemCode = storedCode.ifEmpty { rawCode },
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun setupCommitmentForm() {
        commitmentCamera = ViolationImageCaptureCoordinator(
            caller = this,
            contentResolver = { contentResolver },
            onCaptured = { uri, _ ->
                commitmentImages += uri
                updateCommitmentForm { it.copy(images = commitmentImages.toList(), imageCount = commitmentImages.size) }
            },
            onCaptureFailed = { Toast.makeText(this, "خطا در گرفتن تصویر", Toast.LENGTH_SHORT).show() },
        )
        commitmentViewModel.formData.observe(this) { data ->
            commitmentData = data
            val groups = data.response.billCleaningViolationGroups
            commitmentGroup = groups.firstOrNull { it.value == commitmentGroup?.value } ?: groups.firstOrNull()
            commitmentGroup?.let {
                commitmentViewModel.onViolationGroupIdSelected(it.value)
                if (data.response.billCleaningViolations.isEmpty() &&
                    commitmentViolationsRequestedForGroupId != it.value
                ) {
                    commitmentViolationsRequestedForGroupId = it.value
                    commitmentViewModel.updateViolationGroupSelection(it.value)
                }
            }
            val violations = data.response.billCleaningViolations
            commitmentViolation = violations.firstOrNull { it.value == commitmentViolation?.value } ?: violations.firstOrNull()
            commitmentViolation?.let { commitmentViewModel.onViolationIdSelected(it.value) }
            val itemGroups = data.response.billCleaningItemGroups
            commitmentItemGroup = itemGroups.firstOrNull { it.value == commitmentItemGroup?.value } ?: itemGroups.firstOrNull()
            commitmentItemGroup?.let { commitmentViewModel.onItemGroupIdSelected(it.value) }
            val originItems = data.response.billOriginCleaningItems
            commitmentOriginItem = originItems.firstOrNull { it.value == commitmentOriginItem?.value } ?: originItems.firstOrNull()
            commitmentOriginItem?.let { commitmentViewModel.onOriginItemIdSelected(it.value) }
            renderCommitmentSelectors(loading = false)
        }
        commitmentViewModel.isLoading.observe(this) { renderCommitmentSelectors(loading = it) }
        commitmentViewModel.error.observe(this) { message ->
            if (!message.isNullOrBlank()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                commitmentViewModel.clearError()
            }
        }
        commitmentViewModel.submissionState.observe(this) { state ->
            updateCommitmentForm { it.copy(submitting = state is FormDataViewModel.SubmissionState.Loading) }
            when (state) {
                is FormDataViewModel.SubmissionState.Success -> commitmentSubmitted(state)
                is FormDataViewModel.SubmissionState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    commitmentViewModel.clearSubmissionState()
                }
                else -> Unit
            }
        }
    }

    private fun loadCommitmentForm() {
        val info = controller.missionInfo(controller.currentTrackId)
        if (info == null) {
            Toast.makeText(this, "ابتدا برنامه را دریافت کنید", Toast.LENGTH_SHORT).show()
            return
        }
        val tenantId = billTenantContextStore.getTenantId()
        if (tenantId == null) {
            Toast.makeText(this, "ابتدا محدوده کاری را انتخاب کنید", Toast.LENGTH_SHORT).show()
            return
        }
        // A new form load must start a fresh cascade. Keeping this marker from a
        // previous form made the default group look selected while its violations
        // were never requested until the user selected the group again.
        commitmentViolationsRequestedForGroupId = null
        commitmentViolation = null
        updateCommitmentForm {
            it.copy(
                loading = true,
                address = today.currentStreet.orEmpty(),
                gpsWarning = if (controller.isTracking()) null else "برای ثبت نقص ابتدا بازدید را شروع کنید",
            )
        }
        // Employer and contract are fixed when the work area/mission is created. The inline
        // commitment form does not expose those selectors, so seed the submission state from
        // the persisted mission before loading its filtered form data.
        commitmentViewModel.onOrganSelected(info.organId)
        commitmentViewModel.onContractSelected(info.contractId)
        commitmentViewModel.fetchFormDataWithRequest(FormDataRequest(
            contractIds = listOf(info.contractId), organIds = listOf(info.organId),
            billCleaningViolationGroupIds = emptyList(), billCleaningViolationIds = emptyList(),
            billCleaningItemGroupIds = emptyList(), billCleaningItemIds = emptyList(),
            visitDate = info.visitDate, maxVisitDate = null, minVisitDate = null, isDeleted = false,
            tenantId = tenantId,
            billOriginCleaningItemIds = emptyList(), billCleaningViolationId = 0,
            contractId = info.contractId, organId = info.organId,
        ))
    }

    private fun renderCommitmentSelectors(loading: Boolean) {
        val itemGroups = commitmentData?.response?.billCleaningItemGroups.orEmpty()
        val originItems = commitmentData?.response?.billOriginCleaningItems.orEmpty()
        commitmentSelectorOrder = buildList {
            add(CommitmentSelector.Group)
            add(CommitmentSelector.Violation)
            if (itemGroups.isNotEmpty()) add(CommitmentSelector.ItemGroup)
            if (originItems.isNotEmpty()) add(CommitmentSelector.OriginItem)
        }
        updateCommitmentForm {
            it.copy(
                loading = loading,
                selectors = buildList {
                    add(
                    commitmentSelectorState("گروه نقص", commitmentGroup, commitmentData?.response?.billCleaningViolationGroups.orEmpty(), loading),
                    )
                    add(
                    commitmentSelectorState("عنوان نقص", commitmentViolation, commitmentData?.response?.billCleaningViolations.orEmpty(), loading),
                    )
                    if (itemGroups.isNotEmpty()) add(
                        commitmentSelectorState("فصل فهرست‌بها", commitmentItemGroup, itemGroups, loading),
                    )
                    if (originItems.isNotEmpty()) add(
                        commitmentSelectorState("ردیف فهرست‌بها", commitmentOriginItem, originItems, loading),
                    )
                },
            )
        }
    }

    private fun commitmentSelectorState(
        label: String,
        selected: DropdownItem?,
        options: List<DropdownItem>,
        loading: Boolean,
    ) = ViolationSelectorUiState(
        label = label,
        value = selected?.text,
        loading = loading,
        selectedId = selected?.value,
        options = options.map { ViolationSelectorOption(it.value, it.text) },
    )

    private fun handleCommitmentFormEvent(event: ViolationFormEvent) {
        when (event) {
            ViolationFormEvent.Back -> Unit
            is ViolationFormEvent.CountChanged -> updateCommitmentForm { it.copy(count = normalizeViolationNumericInput(event.value), fieldError = null) }
            is ViolationFormEvent.AddressChanged -> updateCommitmentForm { it.copy(address = event.value, fieldError = null) }
            is ViolationFormEvent.DescriptionChanged -> updateCommitmentForm { it.copy(description = event.value, fieldError = null) }
            is ViolationFormEvent.SelectOption -> openCommitmentSelector(event.index, event.value)
            ViolationFormEvent.CaptureImage -> requestCommitmentCamera()
            is ViolationFormEvent.RemoveImage -> removeCommitmentImage(event.index)
            ViolationFormEvent.Submit -> submitCommitment()
        }
    }

    private fun openCommitmentSelector(index: Int, selectedValue: Int?) {
        val selector = commitmentSelectorOrder.getOrNull(index) ?: return
        val options = when (selector) {
            CommitmentSelector.Group -> commitmentData?.response?.billCleaningViolationGroups
            CommitmentSelector.Violation -> commitmentData?.response?.billCleaningViolations
            CommitmentSelector.ItemGroup -> commitmentData?.response?.billCleaningItemGroups
            CommitmentSelector.OriginItem -> commitmentData?.response?.billOriginCleaningItems
        }.orEmpty()
        if (options.isEmpty()) return Toast.makeText(this, R.string.violation_options_unavailable, Toast.LENGTH_SHORT).show()
        if (selectedValue != null) {
            options.firstOrNull { it.value == selectedValue }?.let { selectCommitmentOption(selector, it) }
            return
        }
        MissionItemSelectionBottomSheet.newInstance(
            "انتخاب ${today.commitmentForm.selectors.getOrNull(index)?.label.orEmpty()}",
            options.map(MissionSelectionOption::fromDropdown),
        ).apply {
            onItemSelected = { selected ->
                options.firstOrNull { it.value == selected.id }?.let { selectCommitmentOption(selector, it) }
            }
        }.show(supportFragmentManager, "commitment_selector")
    }

    private fun selectCommitmentOption(selector: CommitmentSelector, item: DropdownItem) {
        when (selector) {
            CommitmentSelector.Group -> {
                commitmentGroup = item
                commitmentViolation = null
                commitmentViolationsRequestedForGroupId = item.value
                commitmentViewModel.onViolationGroupIdSelected(item.value)
                commitmentViewModel.updateViolationGroupSelection(item.value)
            }
            CommitmentSelector.Violation -> {
                commitmentViolation = item
                commitmentViewModel.onViolationIdSelected(item.value)
                commitmentViewModel.updateViolationSelection(item.value)
            }
            CommitmentSelector.ItemGroup -> {
                commitmentItemGroup = item
                commitmentOriginItem = null
                commitmentViewModel.onItemGroupIdSelected(item.value)
                commitmentViewModel.updateItemGroupSelection(item.value)
            }
            CommitmentSelector.OriginItem -> {
                commitmentOriginItem = item
                commitmentViewModel.onOriginItemIdSelected(item.value)
                commitmentViewModel.updateOriginItemSelection(item.value)
            }
        }
        renderCommitmentSelectors(
            loading = selector == CommitmentSelector.Group || selector == CommitmentSelector.ItemGroup,
        )
    }

    private fun requestCommitmentCamera() {
        val permissions = buildList {
            if (ContextCompat.checkSelfPermission(this@NewSupervisorActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) add(Manifest.permission.CAMERA)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && ContextCompat.checkSelfPermission(this@NewSupervisorActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissions.isEmpty()) commitmentCamera.captureNew() else cameraPermissionLauncher.launch(permissions.toTypedArray())
    }

    private fun removeCommitmentImage(index: Int) {
        commitmentImages.getOrNull(index)?.let { uri ->
            commitmentImages.removeAt(index)
            runCatching { contentResolver.delete(uri, null, null) }
            updateCommitmentForm { it.copy(images = commitmentImages.toList(), imageCount = commitmentImages.size) }
        }
    }

    private fun submitCommitment() {
        val state = today.commitmentForm
        val number = state.count.toIntOrNull() ?: 0
        val error = Ir.co.tfs.farazaman.domain.validation.ViolationFormValidator.validateBasic(number, state.address)
        if (error != null) {
            val message = when (error) {
                Ir.co.tfs.farazaman.domain.validation.ViolationFormValidator.Error.INVALID_COUNT -> "لطفاً تعداد یا مقدار را وارد کنید"
                Ir.co.tfs.farazaman.domain.validation.ViolationFormValidator.Error.EMPTY_ADDRESS -> "لطفاً آدرس را وارد کنید"
            }
            updateCommitmentForm { it.copy(fieldError = message) }
            return
        }
        if (!controller.isTracking()) return Toast.makeText(this, "برای ثبت نقص ابتدا بازدید را شروع کنید", Toast.LENGTH_LONG).show()
        val point = latestTrackPoint() ?: return Toast.makeText(this, "هنوز نقطه GPS ثبت نشده است", Toast.LENGTH_LONG).show()
        val info = controller.missionInfo(controller.currentTrackId) ?: return
        val requiresPriceListSelection = commitmentData?.response?.let {
            it.billCleaningItemGroups.isNotEmpty() && it.billOriginCleaningItems.isNotEmpty()
        } == true
        commitmentViewModel.submitViolation(
            info.visitDate,
            number,
            state.address,
            state.description,
            commitmentImages.toList(),
            point,
            requiresPriceListSelection,
            info.organFenceWkt,
        )
    }

    private fun latestTrackPoint(): TrackPoint? = contentResolver.query(
        TrackContentProvider.trackPointsUri(controller.currentTrackId), null, null, null,
        "${TrackContentProvider.Schema.COL_TIMESTAMP} DESC",
    )?.use { cursor -> if (cursor.moveToFirst()) TrackPoint(cursor) else null }

    private fun commitmentSubmitted(result: FormDataViewModel.SubmissionState.Success) {
        val point = latestTrackPoint()
        val info = controller.missionInfo(controller.currentTrackId)
        Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
        if (point != null && info != null) {
            SupervisorViolationHistoryStore.add(
                context = this,
                title = commitmentGroup?.text ?: getString(R.string.supervisor_row_commitments),
                trackId = controller.currentTrackId,
                violationId = result.violationId,
                subtitle = commitmentViolation?.text.orEmpty(),
                locationName = today.commitmentForm.address,
                organization = info.organTitle,
                contract = info.contractTitle,
                latitude = point.latitude,
                longitude = point.longitude,
            )
            commitmentViewModel.saveViolationAsWaypoint(controller.currentTrackId, point.latitude, point.longitude, result.violationId)
        }
        commitmentImages.clear()
        updateCommitmentForm { it.copy(count = "", description = "", images = emptyList(), imageCount = 0, fieldError = null, submitting = false) }
        commitmentViewModel.clearSubmissionState()
        refreshAll()
    }

    private fun updateCommitmentForm(transform: (ViolationFormUiState) -> ViolationFormUiState) {
        today = today.copy(commitmentForm = transform(today.commitmentForm))
    }

    private fun buildDates(){ val c=Calendar.getInstance().apply{add(Calendar.DAY_OF_YEAR,-3)}; repeat(7){dates+=PersianDate(c.time);c.add(Calendar.DAY_OF_YEAR,1)} }
    private fun refreshHistory(){ val rows=SupervisorViolationHistoryStore.loadForPersianDay(this,selectedDate.shYear,selectedDate.shMonth,selectedDate.shDay); val items=rows.groupBy{it.title to it.subtitle}.map{(k,g)->SupervisorHistoryEntryUiItem("${k.first}|${k.second}",k.first,k.second,g.map{formatTime(it.timestampMs)},emptyList())}; val weekdays=arrayOf("شنبه","یکشنبه","دوشنبه","سه‌شنبه","چهارشنبه","پنج‌شنبه","جمعه"); history=SupervisorHistoryUiState(dates.map{SupervisorHistoryDateUiItem(dateId(it),weekdays[it.dayOfWeek().coerceIn(0,6)],it.shDay.toString(),dateId(it)==dateId(selectedDate),it.dayOfWeek()==6)},items,NotificationUnreadCountStore.read(this)) }
    private fun historyEvent(e:SupervisorHistoryEvent){when(e){SupervisorHistoryEvent.OpenNotifications->openActionsNotifications();SupervisorHistoryEvent.OpenProfile->SupervisorProfileActivity.open(this);is SupervisorHistoryEvent.SelectDate->dates.firstOrNull{dateId(it)==e.id}?.let{selectedDate=it;refreshHistory()}}}
    private fun dateId(d:PersianDate)="${d.shYear}/${d.shMonth}/${d.shDay}"
    private fun formatTime(ms:Long)=SimpleDateFormat("HH:mm",Locale.getDefault()).format(Date(ms))

    fun openViolationForm(){ if(controller.currentTrackId<0) Toast.makeText(this,"ابتدا برنامه را دریافت کنید",Toast.LENGTH_SHORT).show() else ensureCurrentDayMission{controller.openViolationForm()} }
    fun openDailyPlanViolation(id:Long,type:String,title:String){if(isMissionRegisteredForToday(id))TodayVisitMissionActions.openViolation(this,id,type,title)else ensureCurrentDayMission()}
    fun isMissionRegisteredForToday(id:Long)=SupervisorMissionSetup.isMissionRegisteredForDate(this,id)
    fun ensureCurrentDayMission(ready:(()->Unit)?=null){controller.loadState();val id=controller.currentTrackId;if(id<0||isMissionRegisteredForToday(id)){ready?.invoke();return};ready?.let(pendingActions::add);if(dayJob?.isActive==true)return;dayJob=lifecycleScope.launch{try{withContext(Dispatchers.IO){SupervisorMissionSetup.refreshMissionForDateIfNeeded(applicationContext,formDataApiService,id)};onMissionUpdated();pendingActions.toList().also{pendingActions.clear()}.forEach{it()}}catch(c:CancellationException){throw c}catch(t:Throwable){Log.e("Supervisor","day refresh",t)}finally{dayJob=null}}}
    private fun startWatcher(){if(watcherJob?.isActive!=true)watcherJob=lifecycleScope.launch{while(isActive){delay(300_000);ensureCurrentDayMission()}}}
    private fun startTodayRoadWatcher() {
        if (todayRoadWatcherJob?.isActive == true) return
        todayRoadWatcherJob = lifecycleScope.launch {
            while (isActive) {
                delay(TODAY_ROAD_REFRESH_INTERVAL_MS)
                if (tab == SupervisorShellTab.Today && controller.isTracking()) refreshTodayRoadInfo()
            }
        }
    }
    fun syncTrackIdToIntent(){if(controller.currentTrackId>=0)intent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID,controller.currentTrackId)else intent.removeExtra(TrackContentProvider.Schema.COL_TRACK_ID)}
    fun navigateToTab(t:SupervisorNavTab)=selectTab(when(t){SupervisorNavTab.HOME->SupervisorShellTab.Home;SupervisorNavTab.TODAY->SupervisorShellTab.Today;SupervisorNavTab.HISTORY->SupervisorShellTab.History})
    fun openActionsNotifications()=actionsLauncher.launch(Intent(this,ActionsNotificationsActivity::class.java))
    fun showSnackbarAboveNavigation(@StringRes message:Int,duration:Int=Snackbar.LENGTH_LONG){Snackbar.make(findViewById(android.R.id.content),message,duration).show()}
    fun navigateToTodayTab(v:SupervisorVisitTab){today=today.copy(selectedTab=SupervisorTodayTab.entries.getOrElse(v.index){SupervisorTodayTab.Planning});selectTab(SupervisorShellTab.Today)}
    private fun selectTab(t:SupervisorShellTab){tab=t;when(t){SupervisorShellTab.Home->refreshHome();SupervisorShellTab.Today->refreshToday();SupervisorShellTab.History->refreshHistory()}}

    private companion object {
        const val TODAY_ROAD_REFRESH_INTERVAL_MS = 5_000L
        const val BACKGROUND_MAP_RETRY_DELAY_MS = 700L
        const val BACKGROUND_MAP_RETRY_COUNT = 4
    }
}
