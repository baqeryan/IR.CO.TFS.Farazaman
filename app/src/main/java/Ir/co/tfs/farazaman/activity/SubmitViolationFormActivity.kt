package Ir.co.tfs.farazaman.activity

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.data.api.FormDataApiService
import Ir.co.tfs.farazaman.data.db.DatabaseHelper
import Ir.co.tfs.farazaman.data.db.TrackContentProvider
import Ir.co.tfs.farazaman.data.db.model.TrackPoint
import Ir.co.tfs.farazaman.data.model.DropdownItem
import Ir.co.tfs.farazaman.data.model.FormDataRequest
import Ir.co.tfs.farazaman.data.model.FormDataResponse
import Ir.co.tfs.farazaman.data.repository.AddressLookupCoordinator
import Ir.co.tfs.farazaman.domain.validation.ViolationFormValidator
import Ir.co.tfs.farazaman.presentation.base.BaseActivity
import Ir.co.tfs.farazaman.presentation.compose.ViolationFormEvent
import Ir.co.tfs.farazaman.presentation.compose.ViolationFormScreen
import Ir.co.tfs.farazaman.presentation.compose.ViolationFormUiState
import Ir.co.tfs.farazaman.presentation.compose.ViolationSelectorUiState
import Ir.co.tfs.farazaman.presentation.compose.ViolationSelectorOption
import Ir.co.tfs.farazaman.presentation.compose.normalizeViolationNumericInput
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import Ir.co.tfs.farazaman.presentation.viewmodel.FormDataViewModel
import Ir.co.tfs.farazaman.presentation.viewmodel.FormDataViewModel.SubmissionState
import Ir.co.tfs.farazaman.supervisor.DailyPlanItemViolationHelper
import Ir.co.tfs.farazaman.supervisor.SupervisorMissionSetup
import Ir.co.tfs.farazaman.supervisor.SupervisorViolationHistoryStore
import Ir.co.tfs.farazaman.util.AddressLocationHelper
import Ir.co.tfs.farazaman.util.GpsAccuracyHelper
import Ir.co.tfs.farazaman.util.BillTenantContextStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
open class SubmitViolationFormActivity : BaseActivity() {
    protected val formDataViewModel: FormDataViewModel by viewModels()
    @Inject lateinit var addressLookup: AddressLookupCoordinator
    @Inject lateinit var missionFormDataApiService: FormDataApiService
    @Inject lateinit var billTenantContextStore: BillTenantContextStore

    protected open val formTitle: String get() = getString(R.string.violation_form_title)
    protected open val showItemSelectors = true
    protected open val showMissionSelectors = false
    protected open val keepOpenAfterSuccess = false
    protected open val commitmentsMode = false

    private enum class SelectorKey { Organ, Contract, ViolationGroup, Violation, ItemGroup, OriginItem }

    private val uiState = mutableStateOf(ViolationFormUiState())
    private var selectorKeys: List<SelectorKey> = emptyList()
    private val selections = mutableMapOf<SelectorKey, DropdownItem>()
    private var formData: FormDataResponse? = null
    private var defaultsApplied = false
    private var trackId = -1L
    private var latitude = 0.0
    private var longitude = 0.0
    private var lastPoint: TrackPoint? = null
    private var trackActive = false
    private var hasGpsPoints = false
    private var lastAddressCoordinates: Pair<Double, Double>? = null
    private var addressJob: Job? = null
    private var missionJob: Job? = null
    private var visitDate: String? = null
    private var organTitle: String? = null
    private var contractTitle: String? = null
    private var itemTitle: String? = null
    private var presetViolation = 0
    private var presetGroup = 0
    private var violationsRequestedForGroupId: Int? = null
    private var dailyViolations: List<DailyPlanItemViolationHelper.ItemViolation> = emptyList()
    protected val selectedImages = mutableListOf<Uri>()
    private lateinit var camera: ViolationImageCaptureCoordinator
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseIntent()
        setupCamera()
        observeModel()
        setContent { FaraZamanTheme { ViolationFormScreen(uiState.value, ::handleEvent) } }
        requestFormData()
    }

    private fun parseIntent() {
        trackId = intent.getLongExtra(TrackContentProvider.Schema.COL_TRACK_ID, -1L)
        latitude = intent.getDoubleExtra(DisplayTrackMap.EXTRA_LATITUDE, 0.0)
        longitude = intent.getDoubleExtra(DisplayTrackMap.EXTRA_LONGITUDE, 0.0)
        visitDate = intent.getStringExtra("visit_date")
        organTitle = intent.getStringExtra("organ_title")
        contractTitle = intent.getStringExtra("contract_title")
        if (latitude != 0.0 || longitude != 0.0) lastPoint = TrackPoint().apply {
            this.latitude = this@SubmitViolationFormActivity.latitude
            this.longitude = this@SubmitViolationFormActivity.longitude
            pointTimestamp = System.currentTimeMillis()
        }
        intent.getStringExtra("item_data")?.let { raw ->
            runCatching { JSONObject(raw) }.getOrNull()?.let { item ->
                itemTitle = item.optString("billCleaningItemName", "")
                presetViolation = item.optInt("billCleaningViolationID", 0)
                presetGroup = item.optInt("billCleaningViolationGroupId", 0)
                dailyViolations = DailyPlanItemViolationHelper.parseActiveViolations(item)
            }
        }
        uiState.value = buildState(address = intent.getStringExtra("extra_address").orEmpty(), loading = true)
    }

    private fun requestFormData() {
        val organId = intent.getIntExtra("organ_id", -1)
        val contractId = intent.getIntExtra("contract_id", -1)
        val date = visitDate
        val tenantId = billTenantContextStore.getTenantId()
        if (tenantId == null) {
            uiState.value = buildState(loading = false)
            Toast.makeText(this, "ابتدا محدوده کاری را انتخاب کنید", Toast.LENGTH_SHORT).show()
            return
        }
        if (organId > 0 && contractId > 0 && !date.isNullOrBlank()) {
            formDataViewModel.fetchFormDataWithRequest(FormDataRequest(
                contractIds = listOf(contractId), organIds = listOf(organId),
                billCleaningViolationGroupIds = presetGroup.takeIf { it > 0 }?.let(::listOf) ?: emptyList(),
                billCleaningViolationIds = presetViolation.takeIf { it > 0 }?.let(::listOf) ?: emptyList(),
                billCleaningItemGroupIds = emptyList(), billCleaningItemIds = emptyList(), visitDate = date,
                maxVisitDate = null, minVisitDate = null, isDeleted = false,
                tenantId = tenantId,
                billOriginCleaningItemIds = emptyList(), billCleaningViolationId = presetViolation,
                contractId = contractId, organId = organId,
            ))
        } else formDataViewModel.fetchFormData()
    }

    private fun observeModel() {
        formDataViewModel.formData.observe(this) { response ->
            formData = response
            applyDefaults(response)
            uiState.value = buildState(loading = false)
        }
        formDataViewModel.isLoading.observe(this) { uiState.value = buildState(loading = it) }
        formDataViewModel.error.observe(this) { error -> if (!error.isNullOrBlank()) {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show(); formDataViewModel.clearError()
        } }
        formDataViewModel.submissionState.observe(this) { state ->
            uiState.value = buildState(submitting = state is SubmissionState.Loading)
            when (state) {
                is SubmissionState.Success -> submitted(state)
                is SubmissionState.Error -> Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                else -> Unit
            }
        }
    }

    private fun applyDefaults(data: FormDataResponse) {
        val r = data.response
        val usePreset = !defaultsApplied
        choose(SelectorKey.Organ, r.organs, if (usePreset) intent.getIntExtra("organ_id", -1) else -1, if (usePreset) organTitle else null)
        choose(SelectorKey.Contract, r.contracts, if (usePreset) intent.getIntExtra("contract_id", -1) else -1, if (usePreset) contractTitle else null)
        choose(SelectorKey.ViolationGroup, r.billCleaningViolationGroups, if (usePreset) presetGroup else -1)
        choose(SelectorKey.Violation, violationOptions(r.billCleaningViolations), if (usePreset) presetViolation else -1)
        if (showItemSelectors) {
            choose(SelectorKey.ItemGroup, r.billCleaningItemGroups)
            choose(SelectorKey.OriginItem, r.billOriginCleaningItems)
        }
        selections.forEach { (key, item) -> select(key, item, refresh = false) }
        selections[SelectorKey.ViolationGroup]?.let { group ->
            if (r.billCleaningViolations.isEmpty() &&
                violationsRequestedForGroupId != group.value
            ) {
                violationsRequestedForGroupId = group.value
                formDataViewModel.updateViolationGroupSelection(group.value)
            }
        }
        defaultsApplied = true
    }

    private fun choose(key: SelectorKey, options: List<DropdownItem>, id: Int = -1, title: String? = null) {
        val old = selections[key]
        (options.firstOrNull { it.value == id }
            ?: options.firstOrNull { !title.isNullOrBlank() && it.text == title }
            ?: old?.let { value -> options.firstOrNull { it.value == value.value } }
            ?: options.firstOrNull())?.let { selections[key] = it }
    }

    private fun select(key: SelectorKey, item: DropdownItem, refresh: Boolean) {
        selections[key] = item
        when (key) {
            SelectorKey.Organ -> { formDataViewModel.onOrganSelected(item.value); if (refresh) formDataViewModel.updateOrganSelection(item.value) }
            SelectorKey.Contract -> { formDataViewModel.onContractSelected(item.value); if (refresh) formDataViewModel.updateContractSelection(item.value) }
            SelectorKey.ViolationGroup -> {
                formDataViewModel.onViolationGroupIdSelected(item.value)
                if (refresh) {
                    violationsRequestedForGroupId = item.value
                    formDataViewModel.updateViolationGroupSelection(item.value)
                }
            }
            SelectorKey.Violation -> { formDataViewModel.onViolationIdSelected(item.value); if (refresh) formDataViewModel.updateViolationSelection(item.value) }
            SelectorKey.ItemGroup -> { formDataViewModel.onItemGroupIdSelected(item.value); if (refresh) formDataViewModel.updateItemGroupSelection(item.value) }
            SelectorKey.OriginItem -> { formDataViewModel.onOriginItemIdSelected(item.value); if (refresh) formDataViewModel.updateOriginItemSelection(item.value) }
        }
        uiState.value = buildState()
    }

    private fun buildState(
        address: String = uiState.value.address,
        loading: Boolean = uiState.value.loading,
        submitting: Boolean = uiState.value.submitting,
    ): ViolationFormUiState {
        selectorKeys = buildList {
            if (!intent.getBooleanExtra("from_icon_violation", false)) {
                if (showMissionSelectors) {
                    add(SelectorKey.Organ)
                    add(SelectorKey.Contract)
                }
                add(SelectorKey.ViolationGroup)
            }
            add(SelectorKey.Violation)
            if (showItemSelectors) {
                val response = formData?.response
                if (!response?.billCleaningItemGroups.isNullOrEmpty()) add(SelectorKey.ItemGroup)
                if (!response?.billOriginCleaningItems.isNullOrEmpty()) add(SelectorKey.OriginItem)
            }
        }
        val labels = mapOf(
            SelectorKey.Organ to "کارفرما", SelectorKey.Contract to "قرارداد",
            SelectorKey.ViolationGroup to "گروه نقص", SelectorKey.Violation to "عنوان نقص",
            SelectorKey.ItemGroup to "فصل فهرست‌بها", SelectorKey.OriginItem to "ردیف فهرست‌بها",
        )
        return uiState.value.copy(
            title = formTitle,
            selectors = selectorKeys.map { key ->
                ViolationSelectorUiState(
                    label = labels.getValue(key),
                    value = selections[key]?.text,
                    loading = loading,
                    selectedId = selections[key]?.value,
                    options = options(key).map { ViolationSelectorOption(it.value, it.text) },
                )
            },
            address = address,
            imageCount = selectedImages.size,
            images = selectedImages.toList(),
            gpsWarning = gpsWarning(),
            loading = loading, submitting = submitting,
        )
    }

    private fun handleEvent(event: ViolationFormEvent) = when (event) {
        ViolationFormEvent.Back -> finish()
        is ViolationFormEvent.CountChanged -> uiState.value = uiState.value.copy(
            count = normalizeViolationNumericInput(event.value),
            fieldError = null,
        )
        is ViolationFormEvent.AddressChanged -> uiState.value = uiState.value.copy(address = event.value, fieldError = null)
        is ViolationFormEvent.DescriptionChanged -> uiState.value = uiState.value.copy(description = event.value, fieldError = null)
        is ViolationFormEvent.SelectOption -> {
            val key = selectorKeys.getOrNull(event.index)
            val selected = key?.let(::options)?.firstOrNull { it.value == event.value }
            if (key != null && selected != null) select(key, selected, refresh = true)
            else openSelector(event.index)
        }
        ViolationFormEvent.CaptureImage -> requestCamera()
        is ViolationFormEvent.RemoveImage -> removeImage(event.index)
        ViolationFormEvent.Submit -> validateAndSubmit()
    }

    private fun options(key: SelectorKey): List<DropdownItem> {
        val r = formData?.response ?: return emptyList()
        return when (key) {
            SelectorKey.Organ -> r.organs
            SelectorKey.Contract -> r.contracts
            SelectorKey.ViolationGroup -> r.billCleaningViolationGroups
            SelectorKey.Violation -> violationOptions(r.billCleaningViolations)
            SelectorKey.ItemGroup -> r.billCleaningItemGroups
            SelectorKey.OriginItem -> r.billOriginCleaningItems
        }
    }

    private fun violationOptions(fallback: List<DropdownItem>) = if (dailyViolations.isEmpty()) fallback else DailyPlanItemViolationHelper.toDropdownItems(dailyViolations)

    private fun openSelector(index: Int) {
        val key = selectorKeys.getOrNull(index) ?: return
        val values = options(key)
        if (values.isEmpty()) return Toast.makeText(this, R.string.violation_options_unavailable, Toast.LENGTH_SHORT).show()
        MissionItemSelectionBottomSheet.newInstance(uiState.value.selectors[index].label, values.map(MissionSelectionOption::fromDropdown)).apply {
            onItemSelected = { selected -> values.firstOrNull { it.value == selected.id }?.let { select(key, it, true) } }
        }.show(supportFragmentManager, "violation_${key.name}")
    }

    private fun setupCamera() {
        camera = ViolationImageCaptureCoordinator(this, { contentResolver },
            onCaptured = { uri, _ -> selectedImages += uri; uiState.value = buildState() },
            onCaptureFailed = { Toast.makeText(this, "خطا در گرفتن تصویر", Toast.LENGTH_SHORT).show() })
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.isNotEmpty() && result.values.all { it }) camera.captureNew()
            else Toast.makeText(this, "برای استفاده از دوربین، دسترسی لازم است.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestCamera() {
        val needed = buildList {
            if (ContextCompat.checkSelfPermission(this@SubmitViolationFormActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) add(Manifest.permission.CAMERA)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && ContextCompat.checkSelfPermission(this@SubmitViolationFormActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (needed.isEmpty()) camera.captureNew() else permissionLauncher.launch(needed.toTypedArray())
    }

    private fun removeImage(index: Int) {
        val uri = selectedImages.getOrNull(index) ?: return
        selectedImages.removeAt(index)
        runCatching { contentResolver.delete(uri, null, null) }
        uiState.value = buildState()
    }

    private fun validateAndSubmit() {
        val number = uiState.value.count.toIntOrNull() ?: 0
        val error = ViolationFormValidator.validateBasic(number, uiState.value.address)
        if (error != null) {
            uiState.value = uiState.value.copy(fieldError = when (error) {
                ViolationFormValidator.Error.INVALID_COUNT -> "لطفاً تعداد را وارد کنید"
                ViolationFormValidator.Error.EMPTY_ADDRESS -> "لطفاً آدرس را وارد کنید"
            })
            return
        }
        if (!hasGpsPoints) {
            uiState.value = buildState()
            Toast.makeText(this, "برای ثبت نقص ابتدا باید ردیابی GPS فعال و دارای نقطه باشد.", Toast.LENGTH_LONG).show()
            return
        }
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        if (trackId > 0 && prefs.contains("mission_data_$trackId") && !SupervisorMissionSetup.isMissionRegisteredForDate(this, trackId)) refreshMission() else submitNow()
    }

    private fun refreshMission() {
        if (missionJob?.isActive == true) return
        uiState.value = buildState(submitting = true)
        missionJob = lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) { SupervisorMissionSetup.refreshMissionForDateIfNeeded(applicationContext, missionFormDataApiService, trackId) }
                submitNow()
            } catch (cancel: CancellationException) { throw cancel }
            catch (error: Exception) { Log.e(TAG, "Mission refresh failed", error); Toast.makeText(this@SubmitViolationFormActivity, R.string.mission_day_registration_failed, Toast.LENGTH_LONG).show(); uiState.value = buildState(submitting = false) }
            finally { missionJob = null }
        }
    }

    private fun submitNow() = formDataViewModel.submitViolation(
        formatDate(visitDate), uiState.value.count.toInt(), uiState.value.address,
        uiState.value.description,
        selectedImages.toList(),
        lastPoint,
        showItemSelectors && formData?.response?.let {
            it.billCleaningItemGroups.isNotEmpty() && it.billOriginCleaningItems.isNotEmpty()
        } == true,
        missionFenceWkt(),
    )

    private fun missionFenceWkt(): String? {
        if (trackId <= 0) return null
        val rawMission = PreferenceManager.getDefaultSharedPreferences(this)
            .getString("mission_data_$trackId", null)
            ?: return null
        return runCatching { JSONObject(rawMission).optString("organFenceWkt").trim() }
            .getOrNull()
            ?.takeIf(String::isNotEmpty)
    }

    private fun submitted(result: SubmissionState.Success) {
        Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
        if (trackId > 0) SupervisorViolationHistoryStore.add(
            context = this,
            title = historyTitle(),
            trackId = trackId,
            violationId = result.violationId,
            subtitle = historySubtitle(),
            locationName = uiState.value.address,
            organization = organTitle.orEmpty(),
            contract = contractTitle.orEmpty(),
            latitude = latitude,
            longitude = longitude,
        )
        if (trackId > 0 && lastPoint != null) formDataViewModel.saveViolationAsWaypoint(trackId, latitude, longitude, result.violationId)
        if (keepOpenAfterSuccess) {
            selectedImages.clear()
            uiState.value = buildState().copy(count = "", description = "", fieldError = null)
            formDataViewModel.clearSubmissionState()
        } else finish()
    }

    protected open fun historyTitle(): String = if (commitmentsMode) selections[SelectorKey.ViolationGroup]?.text ?: getString(R.string.supervisor_row_commitments) else itemTitle?.takeIf(String::isNotBlank) ?: selections[SelectorKey.Violation]?.text ?: getString(R.string.supervisor_violation_default_title)
    protected open fun historySubtitle(): String = selections[SelectorKey.Violation]?.text.orEmpty().takeUnless { it == historyTitle() }.orEmpty()

    override fun onResume() { super.onResume(); startLocationUpdates() }
    override fun onPause() { addressJob?.cancel(); addressJob = null; super.onPause() }

    private fun startLocationUpdates() {
        if (trackId <= 0 || addressJob?.isActive == true) return
        addressJob = lifecycleScope.launch {
            while (isActive) {
                val snapshot = withContext(Dispatchers.IO) { readGpsSnapshot() }
                trackActive = snapshot.active
                hasGpsPoints = snapshot.hasPoints
                snapshot.point?.let { point ->
                    lastPoint = point; latitude = point.latitude; longitude = point.longitude
                    val coordinates = latitude to longitude
                    if (AddressLocationHelper.shouldUpdateAddress(lastAddressCoordinates, coordinates)) updateAddress(latitude, longitude)
                }
                uiState.value = buildState()
                delay(ADDRESS_INTERVAL)
            }
        }
    }

    private suspend fun updateAddress(lat: Double, lon: Double) {
        if (addressLookup.isCoolingDown()) return
        runCatching {
            val buffer = withContext(Dispatchers.IO) { GpsAccuracyHelper.resolveBuffer(this@SubmitViolationFormActivity, trackId) }
            addressLookup.getRoadData(lat, lon, buffer).firstOrNull()?.name?.trim().orEmpty()
        }.getOrNull()?.takeIf(String::isNotBlank)?.let {
            lastAddressCoordinates = lat to lon
            if (uiState.value.address.isBlank()) uiState.value = buildState(address = it)
        }
    }

    private fun lastTrackPoint(): TrackPoint? = contentResolver.query(TrackContentProvider.trackPointsUri(trackId), null, null, null, "${TrackContentProvider.Schema.COL_TIMESTAMP} DESC")?.use { if (it.moveToFirst()) TrackPoint(it) else null }

    private data class GpsSnapshot(val active: Boolean, val hasPoints: Boolean, val point: TrackPoint?)

    private fun readGpsSnapshot(): GpsSnapshot {
        if (trackId <= 0) return GpsSnapshot(active = lastPoint != null, hasPoints = lastPoint != null, point = lastPoint)
        val active = DatabaseHelper(this).readableDatabase.query(
            TrackContentProvider.Schema.TBL_TRACK, arrayOf(TrackContentProvider.Schema.COL_ACTIVE),
            "${TrackContentProvider.Schema.COL_ID} = ?", arrayOf(trackId.toString()), null, null, null,
        ).use { it.moveToFirst() && it.getInt(0) == TrackContentProvider.Schema.VAL_TRACK_ACTIVE }
        val point = lastTrackPoint()
        return GpsSnapshot(active = active, hasPoints = active && point != null, point = point)
    }

    private fun gpsWarning(): String? = when {
        trackId <= 0 && lastPoint == null -> "⚠️ موقعیت GPS معتبر دریافت نشده است"
        trackActive && hasGpsPoints -> null
        else -> "⚠️ منتظر دریافت موقعیت GPS یا شروع ردیابی هستیم"
    }

    private fun formatDate(value: String?): String {
        val date = value?.trim().orEmpty()
        return when {
            date.isEmpty() -> SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            date.contains('T') -> date.substringBefore('T')
            date.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) -> date
            else -> date
        }
    }

    private companion object { const val TAG = "SubmitViolationForm"; const val ADDRESS_INTERVAL = 1_000L }
}
