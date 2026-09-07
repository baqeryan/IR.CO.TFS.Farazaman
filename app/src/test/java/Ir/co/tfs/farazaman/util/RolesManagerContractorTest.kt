package Ir.co.tfs.farazaman.util

import android.content.SharedPreferences
import Ir.co.tfs.farazaman.data.model.UserRolesResponse
import com.google.gson.Gson
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class RolesManagerContractorTest {

    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var rolesManager: RolesManager
    private var storedRolesJson: String? = null

    @Before
    fun setUp() {
        preferences = Mockito.mock(SharedPreferences::class.java)
        editor = Mockito.mock(SharedPreferences.Editor::class.java)

        Mockito.`when`(preferences.getString(Mockito.eq("user_roles"), Mockito.isNull()))
            .thenAnswer { storedRolesJson }
        Mockito.`when`(preferences.edit()).thenReturn(editor)
        Mockito.`when`(editor.putString(Mockito.eq("user_roles"), Mockito.anyString()))
            .thenAnswer { invocation ->
                storedRolesJson = invocation.getArgument(1)
                editor
            }
        Mockito.`when`(editor.remove(Mockito.eq("user_roles")))
            .thenAnswer {
                storedRolesJson = null
                editor
            }

        rolesManager = RolesManager(preferences, Gson())
    }

    @Test
    fun `legacy roles response without actorRoles remains readable`() {
        storedRolesJson = """{"roles":{"AssetDriver":"راننده"}}"""

        val access = rolesManager.getUserAccess()

        assertTrue(access.hasDriverAccess)
        assertFalse(access.hasSupervisorAccess)
        assertFalse(access.hasContractorAccess)
        assertTrue(access.canAccessOnlyDriver())
    }

    @Test
    fun `Contractor actor role key grants contractor-only access`() {
        rolesManager.saveUserRoles(
            UserRolesResponse(
                actorRoles = mapOf("Contractor" to "پیمانکار"),
            ),
        )

        val access = rolesManager.getUserAccess()

        assertTrue(access.hasContractorAccess)
        assertTrue(access.canAccessOnlyContractor())
        assertFalse(access.hasDriverAccess)
        assertFalse(access.hasSupervisorAccess)
    }

    @Test
    fun `actor role display value is not treated as authorization key`() {
        rolesManager.saveUserRoles(
            UserRolesResponse(
                actorRoles = mapOf("UnconfirmedActor" to "Contractor"),
            ),
        )

        assertFalse(rolesManager.getUserAccess().hasContractorAccess)
    }

    @Test
    fun `Contractor plus legacy role requires role selection`() {
        rolesManager.saveUserRoles(
            UserRolesResponse(
                roles = mapOf("AssetDriver" to "راننده"),
                actorRoles = mapOf("Contractor" to "پیمانکار"),
            ),
        )

        val access = rolesManager.getUserAccess()

        assertTrue(access.hasDriverAccess)
        assertTrue(access.hasContractorAccess)
        assertTrue(access.hasMultipleSelectableRoles())
        assertFalse(access.canAccessOnlyDriver())
        assertFalse(access.canAccessOnlyContractor())
        assertTrue(rolesManager.canSwitchRoles())
    }

    @Test
    fun `Driver and Supervisor interpretation is unchanged when actorRoles is empty`() {
        rolesManager.saveUserRoles(
            UserRolesResponse(
                roles = mapOf(
                    "AssetDriver" to "راننده",
                    "MunicipalContractSupervisor" to "ناظر",
                ),
            ),
        )

        val access = rolesManager.getUserAccess()

        assertTrue(access.hasDriverAccess)
        assertTrue(access.hasSupervisorAccess)
        assertTrue(access.canAccessBoth())
        assertFalse(access.hasContractorAccess)
    }

    @Test
    fun `Driver and Supervisor actor role keys grant supported access`() {
        rolesManager.saveUserRoles(
            UserRolesResponse(
                actorRoles = mapOf(
                    "AssetDriver" to "راننده",
                    "MunicipalContractSupervisor" to "ناظر قرارداد",
                ),
            ),
        )

        val access = rolesManager.getUserAccess()

        assertTrue(access.hasDriverAccess)
        assertTrue(access.hasSupervisorAccess)
        assertTrue(access.canAccessBoth())
    }

    @Test
    fun `no confirmed roles does not grant contractor access`() {
        rolesManager.saveUserRoles(UserRolesResponse())

        val access = rolesManager.getUserAccess()

        assertFalse(access.hasContractorAccess)
        assertFalse(rolesManager.hasAnyRole())
    }

    @Test
    fun `super administrator from legacy roles can access every supported role`() {
        rolesManager.saveUserRoles(
            UserRolesResponse(roles = mapOf("Super Administrator" to "مدیر سامانه")),
        )

        val access = rolesManager.getUserAccess()

        assertTrue(access.isSuperAdmin)
        assertTrue(access.hasSupervisorAccess)
        assertTrue(access.hasDriverAccess)
        assertTrue(access.hasContractorAccess)
    }

}
