package Ir.co.tfs.farazaman.util

import android.content.SharedPreferences
import Ir.co.tfs.farazaman.data.model.UserAccess
import Ir.co.tfs.farazaman.data.model.BusinessActorRole
import Ir.co.tfs.farazaman.data.model.UserRolesResponse
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RolesManager @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) {
    companion object {
        private const val KEY_USER_ROLES = "user_roles"
        private const val ROLE_SUPER_ADMINISTRATOR = "superAdministrator"
        private const val ROLE_SUPER_ADMIN = "superadmin"
    }

    fun saveUserRoles(rolesResponse: UserRolesResponse) {
        val rolesJson = gson.toJson(rolesResponse)
        sharedPreferences.edit().putString(KEY_USER_ROLES, rolesJson).apply()
    }

    fun getUserRoles(): UserRolesResponse? {
        val rolesJson = sharedPreferences.getString(KEY_USER_ROLES, null)
        return rolesJson?.let {
            gson.fromJson(it, UserRolesResponse::class.java)
        }
    }

    fun getUserAccess(): UserAccess {
        val roleResponse = getUserRoles()
        val roles = roleResponse?.roles.orEmpty()
        val actorRoles = roleResponse?.actorRoles.orEmpty()
        val isSuperAdministrator = isSuperAdministrator(roles)

        return UserAccess(
            hasDriverAccess = isSuperAdministrator || hasDriverRoleInMap(roles) ||
                hasActorRole(actorRoles, BusinessActorRole.AssetDriver),
            hasSupervisorAccess = isSuperAdministrator || hasSupervisorRoleInMap(roles) ||
                hasActorRole(actorRoles, BusinessActorRole.MunicipalContractSupervisor),
            isSuperAdmin = isSuperAdministrator,
            hasContractorAccess = isSuperAdministrator ||
                hasActorRole(actorRoles, BusinessActorRole.Contractor),
        )
    }

    fun hasDriverRole(): Boolean {
        val access = getUserAccess()
        return access.hasDriverAccess || access.isSuperAdmin
    }

    fun hasSupervisorRole(): Boolean {
        val access = getUserAccess()
        return access.hasSupervisorAccess || access.isSuperAdmin
    }

    fun hasContractorRole(): Boolean = getUserAccess().hasContractorAccess

    fun canSwitchRoles(): Boolean {
        val access = getUserAccess()
        return access.hasMultipleSelectableRoles()
    }

    fun getProfileRoleLabel(): String? {
        val response = getUserRoles() ?: return null
        return response.roles.values
            .asSequence()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinct()
            .joinToString("، ")
            .takeIf(String::isNotEmpty)
            ?: response.actorRoles.values
                .asSequence()
                .map(String::trim)
                .filter(String::isNotEmpty)
                .distinct()
                .joinToString("، ")
                .takeIf(String::isNotEmpty)
    }

    fun clearUserRoles() {
        sharedPreferences.edit().remove(KEY_USER_ROLES).apply()
    }

    fun hasAnyRole(): Boolean {
        val access = getUserAccess()
        return access.hasDriverAccess || access.hasSupervisorAccess ||
            access.hasContractorAccess || access.isSuperAdmin
    }

    private fun hasDriverRoleInMap(roles: Map<String, String>): Boolean =
        hasRoleInMap(roles, BusinessActorRole.AssetDriver.serviceKey)

    private fun hasSupervisorRoleInMap(roles: Map<String, String>): Boolean =
        hasRoleInMap(roles, BusinessActorRole.MunicipalContractSupervisor.serviceKey)

    private fun hasRoleInMap(roles: Map<String, String>, roleId: String): Boolean {
        return roles.any { (key, value) ->
            key.equals(roleId, ignoreCase = true) || value.equals(roleId, ignoreCase = true)
        }
    }

    private fun isSuperAdministrator(roles: Map<String, String>): Boolean {
        val candidates = roles.keys + roles.values
        return candidates.any { role ->
            val normalized = role.filter(Char::isLetterOrDigit).lowercase()
            normalized == ROLE_SUPER_ADMINISTRATOR.lowercase() || normalized == ROLE_SUPER_ADMIN
        }
    }


    private fun hasActorRole(
        roles: Map<String, String>,
        role: BusinessActorRole,
    ): Boolean = roles.keys.any { it.equals(role.serviceKey, ignoreCase = true) }

}
