package Ir.co.tfs.farazaman.activity

import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.data.api.FormDataApiService
import Ir.co.tfs.farazaman.presentation.compose.SupervisorWorkAreaEvent
import Ir.co.tfs.farazaman.presentation.compose.SupervisorWorkAreaScreen
import Ir.co.tfs.farazaman.presentation.compose.SupervisorWorkAreaUiState
import Ir.co.tfs.farazaman.presentation.compose.SupervisorWorkAreaOption
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import Ir.co.tfs.farazaman.supervisor.SupervisorMissionHelper
import Ir.co.tfs.farazaman.supervisor.SupervisorMissionSetup
import Ir.co.tfs.farazaman.util.ErrorHandler
import Ir.co.tfs.farazaman.util.BillTenantContextStore
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SupervisorWorkAreaSelectionActivity : AppCompatActivity() {
    private companion object { const val TAG = "SupervisorWorkArea" }

    @Inject lateinit var formDataApiService: FormDataApiService
    @Inject lateinit var billTenantContextStore: BillTenantContextStore

    private val uiState = mutableStateOf(SupervisorWorkAreaUiState(loadingContractTypes = true))
    private var selectedTenantId: Int? = null
    private var selectedOrganId: Int? = null
    private var selectedContractId: Int? = null
    private var organOptions: List<MissionSelectionOption> = emptyList()
    private var contractOptions: List<MissionSelectionOption> = emptyList()
    private var contractTypeOptions: List<SupervisorWorkAreaOption> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (SupervisorMissionHelper.hasExistingMission(this)) {
            navigateToHome()
            return
        }

        setContent {
            FaraZamanTheme {
                SupervisorWorkAreaScreen(uiState.value, ::onEvent)
            }
        }
        fetchContractTypes()
    }

    private fun onEvent(event: SupervisorWorkAreaEvent) {
        when (event) {
            SupervisorWorkAreaEvent.OpenProfile -> SupervisorProfileActivity.open(this)
            is SupervisorWorkAreaEvent.SelectContractType -> selectContractType(event.tenantId)
            is SupervisorWorkAreaEvent.SelectOrgan -> selectOrgan(event.id)
            is SupervisorWorkAreaEvent.SelectContract -> selectContract(event.id)
            SupervisorWorkAreaEvent.Submit -> submitSelection()
        }
    }

    private fun fetchContractTypes() {
        uiState.value = uiState.value.copy(loadingContractTypes = true, errorMessage = null)
        lifecycleScope.launch {
            try {
                contractTypeOptions = withContext(Dispatchers.IO) {
                    SupervisorMissionSetup.loadContractTypes(formDataApiService)
                }.map { SupervisorWorkAreaOption(it.tenantId, it.contractTypeName) }
                uiState.value = uiState.value.copy(
                    loadingContractTypes = false,
                    contractTypeOptions = contractTypeOptions,
                )
            } catch (error: Exception) {
                Log.e(TAG, "Unable to load contract types", error)
                uiState.value = uiState.value.copy(
                    loadingContractTypes = false,
                    contractTypeOptions = emptyList(),
                    errorMessage = error.message,
                )
                showMissionError(error, "خطا در دریافت انواع قرارداد")
            }
        }
    }

    private fun selectContractType(tenantId: Int) {
        val option = contractTypeOptions.firstOrNull { it.id == tenantId } ?: return
        selectedTenantId = tenantId
        selectedOrganId = null
        selectedContractId = null
        organOptions = emptyList()
        contractOptions = emptyList()
        uiState.value = uiState.value.copy(
            selectedTenantId = tenantId,
            selectedContractTypeTitle = option.title,
            selectedOrganId = null,
            selectedOrganTitle = null,
            selectedContractId = null,
            selectedContractTitle = null,
            organOptions = emptyList(),
            contractOptions = emptyList(),
        )
        fetchOrgans(tenantId)
    }

    private fun fetchOrgans(tenantId: Int) {
        uiState.value = uiState.value.copy(loadingOrgans = true, errorMessage = null)
        lifecycleScope.launch {
            try {
                organOptions = withContext(Dispatchers.IO) {
                    SupervisorMissionSetup.loadOrgans(formDataApiService, tenantId)
                }
                uiState.value = uiState.value.copy(
                    loadingOrgans = false,
                    organOptions = organOptions.map { SupervisorWorkAreaOption(it.id, it.title) },
                )
            } catch (error: Exception) {
                Log.e(TAG, "Unable to load employer list", error)
                organOptions = emptyList()
                uiState.value = uiState.value.copy(
                    loadingOrgans = false,
                    organOptions = emptyList(),
                    errorMessage = error.message,
                )
                showMissionError(error, "خطا در دریافت لیست کارفرما")
            }
        }
    }

    private fun fetchContracts(organId: Int) {
        val tenantId = selectedTenantId ?: return
        uiState.value = uiState.value.copy(loadingContracts = true, errorMessage = null)
        lifecycleScope.launch {
            try {
                contractOptions = withContext(Dispatchers.IO) {
                    SupervisorMissionSetup.loadContracts(formDataApiService, tenantId, organId)
                }
                uiState.value = uiState.value.copy(
                    loadingContracts = false,
                    contractOptions = contractOptions.map { SupervisorWorkAreaOption(it.id, it.title) },
                )
                if (contractOptions.isEmpty()) {
                    Toast.makeText(this@SupervisorWorkAreaSelectionActivity, R.string.mission_contracts_empty, Toast.LENGTH_SHORT).show()
                }
            } catch (error: Exception) {
                Log.e(TAG, "Unable to load contract list", error)
                contractOptions = emptyList()
                uiState.value = uiState.value.copy(
                    loadingContracts = false,
                    contractOptions = emptyList(),
                    errorMessage = error.message,
                )
                showMissionError(error, "خطا در دریافت لیست قراردادها")
            }
        }
    }

    private fun selectOrgan(id: Int) {
        val option = organOptions.firstOrNull { it.id == id } ?: return
        selectedOrganId = option.id
        selectedContractId = null
        contractOptions = emptyList()
        uiState.value = uiState.value.copy(
            selectedOrganId = option.id,
            selectedOrganTitle = option.title,
            selectedContractId = null,
            selectedContractTitle = null,
            contractOptions = emptyList(),
        )
        fetchContracts(option.id)
    }

    private fun selectContract(id: Int) {
        if (selectedOrganId == null || uiState.value.loadingContracts) return
        val option = contractOptions.firstOrNull { it.id == id } ?: return
        selectedContractId = option.id
        uiState.value = uiState.value.copy(
            selectedContractId = option.id,
            selectedContractTitle = option.title,
        )
    }

    private fun submitSelection() {
        val tenantId = selectedTenantId ?: return
        val organId = selectedOrganId ?: return
        val contractId = selectedContractId ?: return
        val organTitle = uiState.value.selectedOrganTitle ?: return
        val contractTitle = uiState.value.selectedContractTitle ?: return
        uiState.value = uiState.value.copy(submitting = true, errorMessage = null)
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupervisorMissionSetup.createSupervisorMission(
                        context = this@SupervisorWorkAreaSelectionActivity,
                        service = formDataApiService,
                        tenantId = tenantId,
                        organId = organId,
                        contractId = contractId,
                        organTitle = organTitle,
                        contractTitle = contractTitle,
                    )
                }
                billTenantContextStore.saveTenantId(tenantId)
                Toast.makeText(this@SupervisorWorkAreaSelectionActivity, "برنامه کاری با موفقیت ثبت شد", Toast.LENGTH_LONG).show()
                navigateToHome()
            } catch (error: Exception) {
                Log.e(TAG, "Unable to create supervisor mission", error)
                uiState.value = uiState.value.copy(submitting = false, errorMessage = error.message)
                showMissionError(error, "خطا در ثبت برنامه کاری")
            }
        }
    }

    private fun showMissionError(error: Exception, fallback: String) {
        if (error is SupervisorMissionSetup.MissionSetupException) {
            Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
        } else {
            ErrorHandler.handleNetworkError(error, this, fallback)
        }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, NewSupervisorActivity::class.java))
        finish()
    }
}
