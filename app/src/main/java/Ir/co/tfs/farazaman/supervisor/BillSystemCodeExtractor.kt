package Ir.co.tfs.farazaman.supervisor

import org.json.JSONArray
import org.json.JSONObject

/** Finds the bill-of-quantities row code without depending on its nesting in server responses. */
internal fun JSONObject.extractBillSystemCode(): String {
    val objectKeys = keys().asSequence().toList()
    objectKeys.firstOrNull { key ->
        key.filter(Char::isLetterOrDigit).lowercase() in BILL_SYSTEM_CODE_KEYS
    }?.let { key ->
        optString(key).trim().takeIf(String::isNotEmpty)?.let { return it }
    }

    objectKeys.forEach { key ->
        when (val child = opt(key)) {
            is JSONObject -> child.extractBillSystemCode().takeIf(String::isNotEmpty)?.let { return it }
            is JSONArray -> child.extractBillSystemCode().takeIf(String::isNotEmpty)?.let { return it }
        }
    }
    return ""
}

private fun JSONArray.extractBillSystemCode(): String {
    for (index in 0 until length()) {
        when (val child = opt(index)) {
            is JSONObject -> child.extractBillSystemCode().takeIf(String::isNotEmpty)?.let { return it }
            is JSONArray -> child.extractBillSystemCode().takeIf(String::isNotEmpty)?.let { return it }
        }
    }
    return ""
}

private val BILL_SYSTEM_CODE_KEYS = setOf("bilsystemcode", "billsystemcode")
