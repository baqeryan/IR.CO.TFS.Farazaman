package Ir.co.tfs.farazaman.supervisor

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import Ir.co.tfs.farazaman.activity.MissionSelectionOption
import Ir.co.tfs.farazaman.data.api.FormDataApiService
import Ir.co.tfs.farazaman.data.db.TrackContentProvider
import Ir.co.tfs.farazaman.data.model.MissionIndexResponse
import Ir.co.tfs.farazaman.data.model.OrganFenceRequest
import Ir.co.tfs.farazaman.data.model.OrganFenceRequestData
import Ir.co.tfs.farazaman.supervisor.actions.SupervisorActionRecorder
import android.preference.PreferenceManager
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SupervisorMissionSetup {

    private const val TAG = "SupervisorMissionSetup"

    suspend fun loadContractTypes(service: FormDataApiService) = service.getContractTypes().let { response ->
        if (!response.isSuccessful || response.body() == null) {
            throw MissionSetupException("خطا در دریافت انواع قرارداد: ${response.code()}")
        }
        response.body()!!
    }

    suspend fun loadOrgans(service: FormDataApiService, tenantId: Int): List<MissionSelectionOption> {
        val response = service.getAllowedOrgans(tenantId = tenantId)
        if (!response.isSuccessful || response.body() == null) {
            throw MissionSetupException("خطا در دریافت لیست کارفرما: ${response.code()}")
        }
        return response.body()!!.map(MissionSelectionOption::fromDropdown)
    }

    suspend fun loadContracts(service: FormDataApiService, tenantId: Int, organId: Int): List<MissionSelectionOption> {
        val response = service.getAllowedContracts(tenantId = tenantId, organId = organId)
        if (!response.isSuccessful || response.body() == null) {
            throw MissionSetupException("خطا در دریافت لیست قراردادها: ${response.code()}")
        }
        return response.body()!!.map(MissionSelectionOption::fromDropdown)
    }

    suspend fun createSupervisorMission(
        context: Context,
        service: FormDataApiService,
        tenantId: Int,
        organId: Int,
        contractId: Int,
        organTitle: String,
        contractTitle: String,
    ): Long {
        val today = todayVisitDate()
        val missionData = registerDailyPlan(
            service = service,
            tenantId = tenantId,
            organId = organId,
            contractId = contractId,
            visitDate = today,
            organTitle = organTitle,
            contractTitle = contractTitle,
        )
        loadOrganFence(service, organId)?.let { fence ->
            missionData["organFenceWkt"] = fence.wkt
            missionData["organFenceName"] = fence.name
        }
        return persistMission(context, missionData)
    }

    /**
     * Registers the existing, not-yet-uploaded mission for a new local calendar day and
     * replaces its daily-plan payload without creating another GPS track.
     */
    suspend fun refreshMissionForDateIfNeeded(
        context: Context,
        service: FormDataApiService,
        trackId: Long,
        today: String = todayVisitDate(),
    ): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val missionKey = "mission_data_$trackId"
        val stored = prefs.getString(missionKey, null)
            ?: throw MissionSetupException("اطلاعات ماموریت یافت نشد")
        val existing = runCatching { JSONObject(stored) }.getOrElse {
            throw MissionSetupException("اطلاعات ماموریت معتبر نیست")
        }
        if (!SupervisorMissionDayPolicy.requiresRegistration(existing.optString("visitDate"), today)) {
            return false
        }

        val organId = existing.optInt("organId", 0)
        val contractId = existing.optInt("contractId", 0)
        val tenantId = existing.optInt("tenantId", 1)
        if (organId <= 0 || contractId <= 0) {
            throw MissionSetupException("اطلاعات کارفرما یا قرارداد ماموریت کامل نیست")
        }

        val refreshed = registerDailyPlan(
            service = service,
            tenantId = tenantId,
            organId = organId,
            contractId = contractId,
            visitDate = today,
            organTitle = existing.optString("organTitle", ""),
            contractTitle = existing.optString("contractTitle", ""),
        )
        existing.optString("organFenceWkt", "").takeIf { it.isNotBlank() }?.let {
            refreshed["organFenceWkt"] = it
        }
        existing.optString("organFenceName", "").takeIf { it.isNotBlank() }?.let {
            refreshed["organFenceName"] = it
        }

        updatePersistedMission(context, trackId, refreshed)
        Log.i(TAG, "Mission daily plan refreshed for trackId=$trackId date=$today")
        return true
    }

    fun isMissionRegisteredForDate(context: Context, trackId: Long, today: String = todayVisitDate()): Boolean {
        val stored = PreferenceManager.getDefaultSharedPreferences(context)
            .getString("mission_data_$trackId", null)
            ?.let { raw -> runCatching { JSONObject(raw) }.getOrNull() }
            ?.optString("visitDate")
        return !SupervisorMissionDayPolicy.requiresRegistration(stored, today)
    }

    fun todayVisitDate(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private suspend fun registerDailyPlan(
        service: FormDataApiService,
        tenantId: Int,
        organId: Int,
        contractId: Int,
        visitDate: String,
        organTitle: String,
        contractTitle: String,
    ): MutableMap<String, Any> {
        val createResponse = service.createMission(
            tenantId,
            createMissionRequest(tenantId, organId, contractId, visitDate),
        )
        if (!createResponse.isSuccessful) {
            throw MissionSetupException("خطا در ثبت برنامه کاری: ${createResponse.code()}")
        }

        val indexResponse = service.getMissionIndex(
            tenantId,
            createMissionIndexRequest(tenantId, organId, contractId, visitDate),
        )
        if (!indexResponse.isSuccessful || indexResponse.body() == null) {
            throw MissionSetupException("خطا در دریافت جزئیات برنامه کاری: ${indexResponse.code()}")
        }

        return processMissionDataFromResponse(
            missionIndexResponse = indexResponse.body()!!,
            tenantId = tenantId,
            organId = organId,
            contractId = contractId,
            visitDate = visitDate,
            organTitle = organTitle,
            contractTitle = contractTitle,
        ).toMutableMap()
    }

    private fun createMissionRequest(tenantId: Int, organId: Int, contractId: Int, visitDate: String): Map<String, Any> =
        mapOf(
            "request" to missionRequestFields(tenantId, organId, contractId, visitDate),
        )

    private fun createMissionIndexRequest(tenantId: Int, organId: Int, contractId: Int, visitDate: String): Map<String, Any> =
        mapOf(
            "request" to missionRequestFields(tenantId, organId, contractId, visitDate),
            "Pagination" to mapOf("page" to mapOf("pageSize" to 100)),
        )

    private fun missionRequestFields(tenantId: Int, organId: Int, contractId: Int, visitDate: String): Map<String, Any> =
        mapOf(
            "organIds" to listOf(organId),
            "contractIds" to listOf(contractId),
            "visitdate" to visitDate,
            "TenantId" to tenantId,
            "OrganId" to organId,
            "contractId" to contractId,
        )

    private suspend fun loadOrganFence(
        service: FormDataApiService,
        organId: Int,
    ): OrganFence? {
        return try {
            val response = service.getOrganFence(
                OrganFenceRequest(OrganFenceRequestData(organIds = listOf(organId))),
            )
            if (!response.isSuccessful) {
                Log.w(TAG, "Organ fence request failed with code=${response.code()}")
                null
            } else {
                val dataModel = response.body()?.dataModel
                val layer = dataModel?.gisLayersData
                val wkt = layer?.wellKnownTextData?.wellKnownText
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                if (wkt == null) {
                    Log.w(TAG, "Organ fence response did not contain WKT for organId=$organId")
                    null
                } else {
                    OrganFence(
                        wkt = wkt,
                        name = layer.name ?: dataModel.name.orEmpty(),
                    )
                }
            }
        } catch (e: Exception) {
            // The fence is an auxiliary map layer; a temporary failure must not duplicate
            // or block a mission that was already created successfully on the server.
            Log.w(TAG, "Unable to load organ fence for organId=$organId", e)
            null
        }
    }

    private fun processMissionDataFromResponse(
        missionIndexResponse: MissionIndexResponse,
        tenantId: Int,
        organId: Int,
        contractId: Int,
        visitDate: String,
        organTitle: String,
        contractTitle: String,
    ): Map<String, Any> {
        val gson = Gson()
        val jsonString = gson.toJson(missionIndexResponse)
        val fullJsonObject = JSONObject(jsonString)
        val dataArray = fullJsonObject.optJSONArray("data") ?: JSONArray()
        val encryptionsMap = missionIndexResponse.encryptions?.details ?: emptyMap()
        return processMissionData(
            dataArray = dataArray,
            encryptionsMap = encryptionsMap,
            tenantId = tenantId,
            organId = organId,
            contractId = contractId,
            visitDate = visitDate,
            organTitle = organTitle,
            contractTitle = contractTitle,
        )
    }

    private fun processMissionData(
        dataArray: JSONArray,
        encryptionsMap: Map<String, String>,
        tenantId: Int,
        organId: Int,
        contractId: Int,
        visitDate: String,
        organTitle: String,
        contractTitle: String,
    ): Map<String, Any> {
        val planningItems = mutableListOf<Map<String, Any>>()
        val systemItems = mutableListOf<Map<String, Any>>()

        for (i in 0 until dataArray.length()) {
            val item = dataArray.getJSONObject(i)
            val billSystemCode = item.extractBillSystemCode()
            val billCleaningItemName = item.optJSONObject("billOriginCleaningItem")
                ?.optJSONObject("billCleaningItem")
                ?.optString("billCleaningItemName", "") ?: ""
            val appDataProviderID = item.optJSONObject("billOriginCleaningItem")
                ?.optInt("appDataProviderId", 0) ?: 0
            val billOriginCleaningItemRealID = item.optString("billOriginCleaningItemRealID", "")
            val aencryption = encryptionsMap[billOriginCleaningItemRealID] ?: ""
            val billCleaningItemGroupID = item.optJSONObject("billOriginCleaningItem")
                ?.optJSONObject("billCleaningItem")
                ?.optJSONObject("billCleaningItemGroup")
                ?.optInt("billCleaningItemGroupID", 0) ?: 0
            val billCleaningItemGroupName = item.optJSONObject("billOriginCleaningItem")
                ?.optJSONObject("billCleaningItem")
                ?.optJSONObject("billCleaningItemGroup")
                ?.optString("billCleaningItemGroupName", "") ?: ""
            val billCleaningItemID = item.optJSONObject("billOriginCleaningItem")
                ?.optJSONObject("billCleaningItem")
                ?.optInt("billCleaningItemID", 0) ?: 0

            val itemData = mapOf(
                "billCleaningItemName" to billCleaningItemName,
                "bilsystemcode" to billSystemCode,
                "appDataProviderID" to appDataProviderID,
                "billOriginCleaningItemRealID" to billOriginCleaningItemRealID,
                "billCleaningItemGroupID" to billCleaningItemGroupID,
                "billCleaningItemGroupName" to billCleaningItemGroupName,
                "billCleaningItemID" to billCleaningItemID,
                "aencryption" to aencryption,
                "rawData" to item.toString(),
            )

            if (appDataProviderID == 6) {
                planningItems.add(itemData)
            } else {
                systemItems.add(itemData)
            }
        }

        return mapOf(
            "tenantId" to tenantId,
            "organId" to organId,
            "organTitle" to organTitle,
            "contractId" to contractId,
            "contractTitle" to contractTitle,
            "visitDate" to visitDate,
            "planningItems" to planningItems,
            "systemItems" to systemItems,
            "aencryptionsMap" to encryptionsMap,
            "totalItems" to dataArray.length(),
        )
    }

    private fun persistMission(context: Context, missionData: Map<String, Any>): Long {
        val values = ContentValues()
        values.put(TrackContentProvider.Schema.COL_NAME, missionName(missionData))
        val createdAt = Date().time
        values.put(TrackContentProvider.Schema.COL_START_DATE, createdAt)
        values.put(TrackContentProvider.Schema.COL_ACTIVE, TrackContentProvider.Schema.VAL_TRACK_INACTIVE)
        values.put(TrackContentProvider.Schema.COL_ROLE, "supervisor")
        values.putNull(TrackContentProvider.Schema.COL_EXPORT_DATE)

        values.put(TrackContentProvider.Schema.COL_DESCRIPTION, missionDescription(missionData))

        val trackUri = context.contentResolver.insert(TrackContentProvider.CONTENT_URI_TRACK, values)
            ?: throw MissionSetupException("خطا در ایجاد برنامه کاری")
        val trackId = ContentUris.parseId(trackUri)

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putString("mission_data_$trackId", JSONObject(missionData).toString()).apply()
        SupervisorActionRecorder.recordMissionCreated(context, trackId, createdAt, missionData)
        Log.d(TAG, "Mission created with trackId=$trackId")
        return trackId
    }

    private fun updatePersistedMission(context: Context, trackId: Long, missionData: Map<String, Any>) {
        val stored = PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString("mission_data_$trackId", JSONObject(missionData).toString())
            .commit()
        if (!stored) throw MissionSetupException("خطا در ذخیره برنامه کاری روز جدید")

        val values = ContentValues().apply {
            put(TrackContentProvider.Schema.COL_NAME, missionName(missionData))
            put(TrackContentProvider.Schema.COL_DESCRIPTION, missionDescription(missionData))
        }
        context.contentResolver.update(
            ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId),
            values,
            null,
            null,
        )
    }

    private fun missionName(missionData: Map<String, Any>): String =
        "ماموریت ${missionData["organId"]} - ${missionData["contractId"]} - ${missionData["visitDate"]}"

    private fun missionDescription(missionData: Map<String, Any>): String =
        buildString {
            append("Organ ID: ${missionData["organId"]}\n")
            append("Contract ID: ${missionData["contractId"]}\n")
            append("Visit Date: ${missionData["visitDate"]}\n")
            append("Planning Items: ${(missionData["planningItems"] as List<*>).size}\n")
            append("System Items: ${(missionData["systemItems"] as List<*>).size}\n")
            append("Total Items: ${missionData["totalItems"]}")
        }

    class MissionSetupException(message: String) : Exception(message)

    private data class OrganFence(
        val wkt: String,
        val name: String,
    )
}
