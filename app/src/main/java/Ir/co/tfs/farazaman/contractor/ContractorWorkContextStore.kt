package Ir.co.tfs.farazaman.contractor

import android.content.Context
import android.preference.PreferenceManager

data class ContractorWorkContext(
    val organId: Int,
    val organTitle: String,
    val contractId: Int,
    val contractTitle: String,
)

object ContractorWorkContextStore {
    private const val PREFIX = "contractor_work_context_"

    fun read(context: Context): ContractorWorkContext? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val organId = preferences.getInt("${PREFIX}organ_id", -1)
        val contractId = preferences.getInt("${PREFIX}contract_id", -1)
        val organTitle = preferences.getString("${PREFIX}organ_title", null)
        val contractTitle = preferences.getString("${PREFIX}contract_title", null)
        return if (organId > 0 && contractId > 0 && !organTitle.isNullOrBlank() && !contractTitle.isNullOrBlank()) {
            ContractorWorkContext(organId, organTitle, contractId, contractTitle)
        } else {
            null
        }
    }

    fun save(context: Context, workContext: ContractorWorkContext) {
        require(workContext.organId > 0 && workContext.contractId > 0)
        require(workContext.organTitle.isNotBlank() && workContext.contractTitle.isNotBlank())
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putInt("${PREFIX}organ_id", workContext.organId)
            .putString("${PREFIX}organ_title", workContext.organTitle)
            .putInt("${PREFIX}contract_id", workContext.contractId)
            .putString("${PREFIX}contract_title", workContext.contractTitle)
            .apply()
    }

    fun clear(context: Context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .remove("${PREFIX}organ_id")
            .remove("${PREFIX}organ_title")
            .remove("${PREFIX}contract_id")
            .remove("${PREFIX}contract_title")
            .apply()
    }
}
