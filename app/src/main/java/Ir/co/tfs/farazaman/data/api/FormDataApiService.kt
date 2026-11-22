package Ir.co.tfs.farazaman.data.api

import Ir.co.tfs.farazaman.data.model.FormDataApiRequest
import Ir.co.tfs.farazaman.data.model.FormDataResponse
import Ir.co.tfs.farazaman.data.model.DropdownItem
import Ir.co.tfs.farazaman.data.model.ContractTypeTenantOption
import Ir.co.tfs.farazaman.data.model.MissionIndexResponse
import Ir.co.tfs.farazaman.data.model.OrganFenceRequest
import Ir.co.tfs.farazaman.data.model.OrganFenceResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FormDataApiService {
    @GET("api/Bill/Lookup/ContractType")
    suspend fun getContractTypes(): Response<List<ContractTypeTenantOption>>

    @GET("api/Bill/Lookup/Organs")
    suspend fun getAllowedOrgans(
        @Query("tenantId") tenantId: Int
    ): Response<List<DropdownItem>>

    @GET("api/Bill/Lookup/Contracts")
    suspend fun getAllowedContracts(
        @Query("tenantId") tenantId: Int,
        @Query("organId") organId: Int? = null
    ): Response<List<DropdownItem>>

    @POST("api/Bill/Lookup/ViolationGroups")
    suspend fun getViolationGroups(@Body request: FormDataApiRequest): Response<FormDataResponse>

    @POST("api/Bill/Lookup/Violations")
    suspend fun getViolations(@Body request: FormDataApiRequest): Response<FormDataResponse>

    @POST("api/Bill/Lookup/ItemGroups")
    suspend fun getItemGroups(@Body request: FormDataApiRequest): Response<FormDataResponse>

    @POST("api/Bill/Lookup/OriginItems")
    suspend fun getOriginItems(@Body request: FormDataApiRequest): Response<FormDataResponse>

    @POST("api/Bill/Lookup/ViolationDetails")
    suspend fun getViolationDetails(@Body request: FormDataApiRequest): Response<FormDataResponse>
    
    /**
     * Upload GPS track file
     */
    @POST("api/GisGeolocation/upload")
    suspend fun uploadTrackFile(
        @Body request: Any
    ): Response<Any>
    
    /**
     * Get mission details by encryption
     */
    @POST("api/BillOriginCleaningItemDailyItem/Details/{encryption}")
    suspend fun getMissionDetails(
        @Path("encryption") encryption: String,
        @Body request: Any = mapOf("request" to mapOf("TenantId" to 1))
    ): Response<Any>
    
    /**
     * Create new mission
     */
    @POST("api/BillOriginCleaningItemDailyItem/Create")
    suspend fun createMission(
        @Query("tenantId") tenantId: Int,
        @Body request: Any
    ): Response<Any>
    
    /**
     * Get mission index/details after creation
     */
    @POST("api/BillOriginCleaningItemDailyItem/Index")
    suspend fun getMissionIndex(
        @Query("tenantId") tenantId: Int,
        @Body request: Any
    ): Response<MissionIndexResponse>

    @Headers("Accept: application/octet-stream")
    @POST("api/GisLayersDataFenceOrgan/Details")
    suspend fun getOrganFence(
        @Body request: OrganFenceRequest
    ): Response<OrganFenceResponse>
} 
