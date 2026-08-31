package Ir.co.tfs.farazaman.data.model

import com.google.gson.annotations.SerializedName

data class OrganFenceRequest(
    @SerializedName("request") val request: OrganFenceRequestData,
)

data class OrganFenceRequestData(
    @SerializedName("organIds") val organIds: List<Int>,
)

data class OrganFenceResponse(
    @SerializedName("dataModel") val dataModel: OrganFenceDataModel? = null,
)

data class OrganFenceDataModel(
    @SerializedName("gisLayersData") val gisLayersData: OrganFenceLayerData? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("organId") val organId: Int? = null,
)

data class OrganFenceLayerData(
    @SerializedName("gisLayersDataWellKnownText")
    val wellKnownTextData: OrganFenceWellKnownText? = null,
    @SerializedName("name") val name: String? = null,
)

data class OrganFenceWellKnownText(
    @SerializedName("wellKnownText") val wellKnownText: String? = null,
)
