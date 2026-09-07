package Ir.co.tfs.farazaman.notification

import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import org.json.JSONArray
import org.json.JSONObject

data class FirebaseNotificationRecord(
    val id: String,
    val title: String,
    val body: String,
    val receivedAtMs: Long,
    val data: Map<String, String>,
)

object FirebaseNotificationInboxStore {
    const val ACTION_INBOX_CHANGED = "Ir.co.tfs.farazaman.notification.INBOX_CHANGED"
    private const val KEY_PREFIX = "firebase_notification_inbox_"
    private const val USER_NAME_KEY = "user_name"
    private const val MAX_RECORDS = 200

    @Synchronized
    fun upsert(context: Context, record: FirebaseNotificationRecord) {
        val records = load(context).toMutableList()
        records.removeAll { it.id == record.id }
        records.add(record)
        save(context, records.sortedByDescending { it.receivedAtMs }.take(MAX_RECORDS))
        context.sendBroadcast(Intent(ACTION_INBOX_CHANGED).setPackage(context.packageName))
    }

    fun ingestNotificationIntent(context: Context, intent: Intent) {
        val extras = intent.extras ?: return
        val data = buildMap {
            extras.keySet().forEach { key ->
                if (!key.startsWith("google.") && !key.startsWith("gcm.") && key != "from") {
                    extras.get(key)?.toString()?.let { put(key, it) }
                }
            }
        }
        val messageId = data["notificationId"]
            ?: extras.getString("google.message_id")
            ?: return
        val title = data["title"]
            ?: extras.getString("gcm.n.title")
            ?: extras.getString("gcm.notification.title")
            ?: ""
        val body = data["body"]
            ?: extras.getString("gcm.n.body")
            ?: extras.getString("gcm.notification.body")
            ?: ""
        val sentAt = extras.getLong("google.sent_time").takeIf { it > 0L }
            ?: System.currentTimeMillis()
        upsert(
            context,
            FirebaseNotificationRecord(messageId, title, body, sentAt, data),
        )
    }

    @Synchronized
    fun load(context: Context): List<FirebaseNotificationRecord> {
        val raw = preferences(context).getString(storageKey(context), null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val dataObject = item.optJSONObject("data") ?: JSONObject()
                    val data = buildMap {
                        dataObject.keys().forEach { key ->
                            if (!dataObject.isNull(key)) put(key, dataObject.optString(key))
                        }
                    }
                    add(
                        FirebaseNotificationRecord(
                            id = item.optString("id"),
                            title = item.optString("title"),
                            body = item.optString("body"),
                            receivedAtMs = item.optLong("receivedAtMs"),
                            data = data,
                        ),
                    )
                }
            }.sortedByDescending { it.receivedAtMs }
        }.getOrDefault(emptyList())
    }

    private fun save(context: Context, records: List<FirebaseNotificationRecord>) {
        val array = JSONArray()
        records.forEach { record ->
            array.put(JSONObject().apply {
                put("id", record.id)
                put("title", record.title)
                put("body", record.body)
                put("receivedAtMs", record.receivedAtMs)
                put("data", JSONObject(record.data))
            })
        }
        preferences(context).edit().putString(storageKey(context), array.toString()).apply()
    }

    private fun preferences(context: Context) =
        PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    private fun storageKey(context: Context): String {
        val user = preferences(context).getString(USER_NAME_KEY, null)
            ?.trim()?.lowercase().orEmpty().ifBlank { "signed_in_user" }
        return KEY_PREFIX + user
    }
}
