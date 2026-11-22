package Ir.co.tfs.farazaman.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillTenantContextStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val preferences = context.getSharedPreferences("bill_tenant_context", Context.MODE_PRIVATE)

    fun getTenantId(): Int? = preferences.getInt(KEY_TENANT_ID, 0).takeIf { it > 0 }

    fun saveTenantId(tenantId: Int) {
        require(tenantId > 0) { "tenantId must be greater than zero" }
        preferences.edit().putInt(KEY_TENANT_ID, tenantId).apply()
    }

    fun clear() {
        preferences.edit().remove(KEY_TENANT_ID).apply()
    }

    private companion object {
        const val KEY_TENANT_ID = "tenant_id"
    }
}
