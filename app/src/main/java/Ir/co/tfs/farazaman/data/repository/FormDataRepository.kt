package Ir.co.tfs.farazaman.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import Ir.co.tfs.farazaman.data.api.FormDataApiService
import Ir.co.tfs.farazaman.data.model.FormDataApiRequest
import Ir.co.tfs.farazaman.data.model.FormDataRequest
import Ir.co.tfs.farazaman.data.model.FormDataResponse
import Ir.co.tfs.farazaman.data.model.RequestData
import Ir.co.tfs.farazaman.util.TokenManager
import retrofit2.Response
import javax.inject.Inject

class FormDataRepository @Inject constructor(
    private val apiService: FormDataApiService,
    private val context: Context,
    private val sharedPreferences: SharedPreferences,
    private val tokenManager: TokenManager
) {
    
    suspend fun getViolationGroups(request: FormDataRequest): Response<FormDataResponse> =
        apiService.getViolationGroups(FormDataApiRequest(request))

    suspend fun getViolations(request: FormDataRequest): Response<FormDataResponse> =
        apiService.getViolations(FormDataApiRequest(request))

    suspend fun getItemGroups(request: FormDataRequest): Response<FormDataResponse> =
        apiService.getItemGroups(FormDataApiRequest(request))

    suspend fun getOriginItems(request: FormDataRequest): Response<FormDataResponse> =
        apiService.getOriginItems(FormDataApiRequest(request))

    suspend fun getViolationDetails(request: FormDataRequest): Response<FormDataResponse> =
        apiService.getViolationDetails(FormDataApiRequest(request))
} 
