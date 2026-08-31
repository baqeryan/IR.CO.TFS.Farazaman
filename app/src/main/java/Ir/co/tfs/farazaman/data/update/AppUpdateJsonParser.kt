package Ir.co.tfs.farazaman.data.update

import Ir.co.tfs.farazaman.data.model.AppUpdateInfo
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import javax.inject.Inject

class AppUpdateJsonParser @Inject constructor() {
    fun parse(json: String): AppUpdateInfo {
        val root = JsonParser.parseString(json)
        require(root.isJsonObject) { "Update response must be a JSON object" }
        val obj = root.asJsonObject

        val versionCode = obj.requiredLong("versionCode")
        require(versionCode > 0) { "Invalid update versionCode" }
        val versionName = obj.requiredString("versionName")
        val downloadUrl = obj.requiredString("downloadUrl")
        require(AppUpdateConfig.isTrustedDownloadUrl(downloadUrl)) {
            "Untrusted update download URL"
        }

        return AppUpdateInfo(
            versionCode = versionCode,
            versionName = versionName,
            downloadUrl = downloadUrl,
            mandatory = obj.requiredBoolean("mandatory"),
            releaseNotes = obj.optionalString("releaseNotes"),
            publishedAt = obj.optionalString("publishedAt"),
        )
    }

    private fun JsonObject.requiredString(name: String): String {
        val value = get(name)
        require(value != null && value.isJsonPrimitive && value.asJsonPrimitive.isString) {
            "Missing or invalid $name"
        }
        return value.asString.trim().also { require(it.isNotEmpty()) { "$name is empty" } }
    }

    private fun JsonObject.optionalString(name: String): String? {
        val value = get(name) ?: return null
        if (value.isJsonNull) return null
        require(value.isJsonPrimitive && value.asJsonPrimitive.isString) { "Invalid $name" }
        return value.asString.trim().takeIf(String::isNotEmpty)
    }

    private fun JsonObject.requiredLong(name: String): Long {
        val value = get(name)
        require(value != null && value.isJsonPrimitive && value.asJsonPrimitive.isNumber) {
            "Missing or invalid $name"
        }
        return value.asLong
    }

    private fun JsonObject.requiredBoolean(name: String): Boolean {
        val value = get(name)
        require(value != null && value.isJsonPrimitive && value.asJsonPrimitive.isBoolean) {
            "Missing or invalid $name"
        }
        return value.asBoolean
    }
}
