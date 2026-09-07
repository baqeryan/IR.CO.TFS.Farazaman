package Ir.co.tfs.farazaman.supervisor.actions

import android.content.Context
import android.preference.PreferenceManager
import org.json.JSONArray
import org.json.JSONObject

/** A bounded, per-user local source until a unified activity-feed backend contract exists. */
object UserActionStore {
    private const val ACTIONS_KEY_PREFIX = "user_action_feed_"
    private const val BOOTSTRAP_KEY_PREFIX = "user_action_feed_bootstrapped_"
    private const val USER_NAME_KEY = "user_name"
    private const val MAX_RECORDS = 500

    @Synchronized
    fun upsert(context: Context, record: UserActionRecord) {
        val records = load(context).toMutableList()
        records.removeAll { it.id == record.id }
        records.add(record)
        save(context, records.sortedByDescending { it.timestampMs }.take(MAX_RECORDS))
    }

    @Synchronized
    fun load(context: Context): List<UserActionRecord> {
        val raw = preferences(context).getString(actionsKey(context), null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    add(item.toRecord())
                }
            }.sortedByDescending { it.timestampMs }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun isBootstrapComplete(context: Context): Boolean =
        preferences(context).getBoolean(bootstrapKey(context), false)

    fun markBootstrapComplete(context: Context) {
        preferences(context).edit().putBoolean(bootstrapKey(context), true).apply()
    }

    private fun save(context: Context, records: List<UserActionRecord>) {
        val array = JSONArray()
        records.forEach { record -> array.put(record.toJson()) }
        preferences(context).edit().putString(actionsKey(context), array.toString()).apply()
    }

    private fun UserActionRecord.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("type", type)
        put("title", title)
        put("description", description)
        put("timestampMs", timestampMs)
        put("category", category)
        put("iconKey", iconKey)
        put("status", status)
        put("relatedEntityType", relatedEntityType)
        put("relatedEntityId", relatedEntityId)
        put("metadata", JSONObject(metadata))
        put("operationKeys", JSONArray(operationKeys))
    }

    private fun JSONObject.toRecord(): UserActionRecord {
        val metadataObject = optJSONObject("metadata") ?: JSONObject()
        val metadata = buildMap {
            metadataObject.keys().forEach { key ->
                if (!metadataObject.isNull(key)) put(key, metadataObject.getString(key))
            }
        }
        val operationsArray = optJSONArray("operationKeys") ?: JSONArray()
        val operations = buildList {
            for (index in 0 until operationsArray.length()) {
                operationsArray.optString(index).takeIf { it.isNotBlank() }?.let(::add)
            }
        }
        return UserActionRecord(
            id = optString("id"),
            type = optString("type"),
            title = optString("title"),
            description = optString("description"),
            timestampMs = optLong("timestampMs"),
            category = optString("category", UserActionCategories.OTHER),
            iconKey = optNullableString("iconKey"),
            status = optNullableString("status"),
            relatedEntityType = optNullableString("relatedEntityType"),
            relatedEntityId = optNullableString("relatedEntityId"),
            metadata = metadata,
            operationKeys = operations,
        )
    }

    private fun JSONObject.optNullableString(key: String): String? =
        if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }

    private fun preferences(context: Context) =
        PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    private fun actionsKey(context: Context): String = ACTIONS_KEY_PREFIX + userScope(context)

    private fun bootstrapKey(context: Context): String = BOOTSTRAP_KEY_PREFIX + userScope(context)

    private fun userScope(context: Context): String =
        preferences(context).getString(USER_NAME_KEY, null)?.trim()?.lowercase().orEmpty()
            .ifBlank { "signed_in_user" }
}
