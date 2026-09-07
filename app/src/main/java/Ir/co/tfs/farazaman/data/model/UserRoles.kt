package Ir.co.tfs.farazaman.data.model

import com.google.gson.annotations.SerializedName

enum class BusinessActorRole(val displayNameFa: String) {
    MunicipalContractSupervisor("ناظر قرارداد"),
    MunicipalContractInspector("بازرس قرارداد"),
    AssetDriver("راننده"),
    Contractor("پیمانکار"),
    WorkStationOperator("اپراتور ایستگاه"),
    Citizen("شهروند"),
    ContractorManagingDirector("مدیرعامل"),
    PropertyOwner("مالک ملک"),
    AssetVehicleOwner("مالک خودرو"),
    ;

    val serviceKey: String
        get() = name

    companion object {
        fun fromServiceKey(key: String): BusinessActorRole? =
            entries.firstOrNull { it.serviceKey.equals(key, ignoreCase = true) }
    }
}

data class UserRolesResponse(
    @SerializedName("roles")
    val roles: Map<String, String> = emptyMap(),
    @SerializedName("actorRoles")
    val actorRoles: Map<String, String> = emptyMap(),
) {
    fun businessActorRoles(): Set<BusinessActorRole> =
        actorRoles.keys.mapNotNull(BusinessActorRole::fromServiceKey).toSet()
}

// Helper class to determine user access based on roles
data class UserAccess(
    val hasDriverAccess: Boolean,
    val hasSupervisorAccess: Boolean,
    val isSuperAdmin: Boolean,
    val hasContractorAccess: Boolean = false,
) {
    fun canAccessBoth(): Boolean = isSuperAdmin || (hasDriverAccess && hasSupervisorAccess)
    fun canAccessOnlyDriver(): Boolean =
        hasDriverAccess && !hasSupervisorAccess && !hasContractorAccess && !isSuperAdmin

    fun canAccessOnlySupervisor(): Boolean =
        hasSupervisorAccess && !hasDriverAccess && !hasContractorAccess && !isSuperAdmin

    fun canAccessOnlyContractor(): Boolean =
        hasContractorAccess && !hasDriverAccess && !hasSupervisorAccess && !isSuperAdmin

    fun selectableRoleCount(): Int {
        val legacyRoleCount = if (isSuperAdmin) {
            2
        } else {
            listOf(hasDriverAccess, hasSupervisorAccess).count { it }
        }
        return legacyRoleCount + if (hasContractorAccess) 1 else 0
    }

    fun hasMultipleSelectableRoles(): Boolean = selectableRoleCount() > 1
}

