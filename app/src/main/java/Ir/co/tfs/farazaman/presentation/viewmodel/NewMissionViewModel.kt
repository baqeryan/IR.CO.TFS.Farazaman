package Ir.co.tfs.farazaman.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import Ir.co.tfs.farazaman.data.api.FormDataApiService
import Ir.co.tfs.farazaman.data.repository.FormDataRepository
import javax.inject.Inject
import Ir.co.tfs.farazaman.util.BillTenantContextStore

@HiltViewModel
class NewMissionViewModel @Inject constructor(
    private val formDataRepository: FormDataRepository,
    private val formDataApiService: FormDataApiService,
    private val billTenantContextStore: BillTenantContextStore,
) : ViewModel() {

    companion object {
        private const val TAG = "NewMissionViewModel"
    }

    // UI State for form data
    private val _formData = MutableStateFlow<Any?>(null)
    val formData: StateFlow<Any?> = _formData.asStateFlow()

    // UI State for organs list
    private val _organs = MutableStateFlow<List<OrganItem>>(emptyList())
    val organs: StateFlow<List<OrganItem>> = _organs.asStateFlow()

    // UI State for contracts list  
    private val _contracts = MutableStateFlow<List<ContractItem>>(emptyList())
    val contracts: StateFlow<List<ContractItem>> = _contracts.asStateFlow()

    // UI State for loading states
    private val _isLoadingFormData = MutableStateFlow(false)
    val isLoadingFormData: StateFlow<Boolean> = _isLoadingFormData.asStateFlow()

    private val _isCreatingMission = MutableStateFlow(false)
    val isCreatingMission: StateFlow<Boolean> = _isCreatingMission.asStateFlow()

    // UI State for errors and success
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // UI State for mission creation result
    private val _missionCreated = MutableStateFlow(false)
    val missionCreated: StateFlow<Boolean> = _missionCreated.asStateFlow()

    /**
     * Load form data including organs and contracts
     */
    fun loadFormData() {
        viewModelScope.launch {
            _isLoadingFormData.value = true
            _errorMessage.value = null
            
            try {
                Log.d(TAG, "Loading form data...")
                val tenantId = requireNotNull(billTenantContextStore.getTenantId()) { "نوع قرارداد انتخاب نشده است." }
                val response = formDataApiService.getAllowedOrgans(tenantId = tenantId)
                
                if (response.isSuccessful) {
                    _organs.value = response.body().orEmpty().map { OrganItem(it.value, it.text) }
                    _contracts.value = emptyList()
                    
                    Log.d(TAG, "Form data loaded successfully")
                } else {
                    Log.e(TAG, "Failed to load form data: ${response.code()}")
                    _errorMessage.value = "خطا در دریافت اطلاعات فرم"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while loading form data", e)
                _errorMessage.value = "خطا در دریافت اطلاعات فرم"
            } finally {
                _isLoadingFormData.value = false
            }
        }
    }

    /**
     * Create a new mission
     */
    fun createMission(organId: Int, contractId: Int, visitDate: String) {
        viewModelScope.launch {
            _isCreatingMission.value = true
            _errorMessage.value = null
            
            try {
                Log.d(TAG, "Creating mission: organId=$organId, contractId=$contractId, visitDate=$visitDate")
                
                val tenantId = requireNotNull(billTenantContextStore.getTenantId()) { "نوع قرارداد انتخاب نشده است." }
                val requestBody = createMissionRequestBody(tenantId, organId, contractId, visitDate)
                val response = formDataApiService.createMission(tenantId, requestBody)
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Mission created successfully")
                    _successMessage.value = "برنامه کاری جدید با موفقیت ثبت شد!"
                    _missionCreated.value = true
                    
                    // Fetch mission details after creation
                    fetchMissionDetails(organId, contractId, visitDate)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Failed to create mission: ${response.code()} - $errorBody")
                    _errorMessage.value = Ir.co.tfs.farazaman.util.ErrorHandler.messageFromBody(
                        response.code(),
                        errorBody,
                        "خطا در ثبت ماموریت جدید",
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while creating mission", e)
                _errorMessage.value = "خطا در ثبت ماموریت جدید"
            } finally {
                _isCreatingMission.value = false
            }
        }
    }

    /**
     * Fetch mission details after creation
     */
    private fun fetchMissionDetails(organId: Int, contractId: Int, visitDate: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Fetching mission details...")
                
                val requestBody = createIndexRequestBody(organId, contractId, visitDate)
                // This would need to call the Index endpoint
                // For now, just log the attempt
                Log.d(TAG, "Mission details request prepared")
                
            } catch (e: Exception) {
                Log.e(TAG, "Exception while fetching mission details", e)
            }
        }
    }

    /**
     * Extract organs from form data response
     */
    private fun extractOrgans(formData: Any): List<OrganItem> {
        // This would need to be implemented based on the actual FormDataResponse structure
        // For now, return empty list
        return emptyList()
    }

    /**
     * Extract contracts from form data response
     */
    private fun extractContracts(formData: Any): List<ContractItem> {
        // This would need to be implemented based on the actual FormDataResponse structure
        // For now, return empty list
        return emptyList()
    }

    /**
     * Create request body for mission creation
     */
    private fun createMissionRequestBody(tenantId: Int, organId: Int, contractId: Int, visitDate: String): Any {
        return mapOf(
            "request" to mapOf(
                "organIds" to listOf(organId),
                "contractIds" to listOf(contractId),
                "visitdate" to visitDate,
                "TenantId" to tenantId,
                "OrganId" to organId,
                "contractId" to contractId
            )
        )
    }

    /**
     * Create request body for mission index/details
     */
    private fun createIndexRequestBody(organId: Int, contractId: Int, visitDate: String): Any {
        val tenantId = requireNotNull(billTenantContextStore.getTenantId()) { "نوع قرارداد انتخاب نشده است." }
        return mapOf(
            "request" to mapOf(
                "organIds" to listOf(organId),
                "contractIds" to listOf(contractId),
                "visitdate" to visitDate,
                "TenantId" to tenantId,
                "OrganId" to organId,
                "contractId" to contractId
            ),
            "Pagination" to mapOf(
                "page" to mapOf("pageSize" to 100)
            )
        )
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Clear success message
     */
    fun clearSuccess() {
        _successMessage.value = null
    }

    /**
     * Reset mission created state
     */
    fun resetMissionCreated() {
        _missionCreated.value = false
    }
}

// Data classes for UI state
data class OrganItem(
    val id: Int,
    val name: String
)

data class ContractItem(
    val id: Int,
    val name: String,
    val organId: Int
)

