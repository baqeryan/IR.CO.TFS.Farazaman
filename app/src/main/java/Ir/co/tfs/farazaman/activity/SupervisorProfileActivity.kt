package Ir.co.tfs.farazaman.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.presentation.compose.SupervisorProfileEvent
import Ir.co.tfs.farazaman.presentation.compose.SupervisorProfileScreen
import Ir.co.tfs.farazaman.presentation.compose.SupervisorProfileUiState
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzButtonVariant
import Ir.co.tfs.farazaman.service.remote.RolesService
import Ir.co.tfs.farazaman.util.AuthHelper
import Ir.co.tfs.farazaman.util.RolesManager
import Ir.co.tfs.farazaman.util.TokenManager
import Ir.co.tfs.farazaman.util.UserManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

@AndroidEntryPoint
class SupervisorProfileActivity : AppCompatActivity() {

    @Inject lateinit var authHelper: AuthHelper
    @Inject lateinit var userManager: UserManager
    @Inject lateinit var rolesManager: RolesManager
    @Inject lateinit var tokenManager: TokenManager
    @Inject lateinit var okHttpClient: OkHttpClient

    private var profileState by mutableStateOf(SupervisorProfileUiState("پروفایل", null, null, false))
    private var showLogoutConfirmation by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FaraZamanTheme {
            SupervisorProfileScreen(profileState, ::handleProfileEvent)
            if (showLogoutConfirmation) AlertDialog(
                onDismissRequest = { showLogoutConfirmation = false },
                title = { Text(getString(R.string.prefs_logout_dialog_title)) },
                text = { Text(getString(R.string.prefs_logout_dialog_message)) },
                confirmButton = { FzButton(getString(R.string.prefs_logout_dialog_confirm), { showLogoutConfirmation = false; performLogout() }) },
                dismissButton = { FzButton(getString(R.string.prefs_logout_dialog_cancel), { showLogoutConfirmation = false }, variant = FzButtonVariant.Ghost) },
            )
        } }
        bindUserInfo()
        setupChangeRoleButton()
        refreshRolesIfNeeded()
    }

    private fun handleProfileEvent(event: SupervisorProfileEvent) {
        when (event) {
            SupervisorProfileEvent.Back -> finish()
            SupervisorProfileEvent.ChangeRole -> startActivity(RoleSelectionActivity.createIntent(this, true, RoleSelectionActivity.ROLE_SUPERVISOR))
            SupervisorProfileEvent.Logout -> confirmLogout()
        }
    }

    private fun refreshRolesIfNeeded() {
        if (rolesManager.canSwitchRoles()) return
        lifecycleScope.launch {
            val token = tokenManager.getAccessToken() ?: return@launch
            val prefs = PreferenceManager.getDefaultSharedPreferences(this@SupervisorProfileActivity)
            val baseUrl = prefs.getString("BASE_URL", "https://app.tfs.co.ir") ?: "https://app.tfs.co.ir"
            val normalizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            val rolesService = Retrofit.Builder()
                .baseUrl(normalizedBaseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RolesService::class.java)

            try {
                val response = withContext(Dispatchers.IO) {
                    rolesService.getUserRoles("Bearer $token").execute()
                }
                if (response.isSuccessful) {
                    response.body()?.let {
                        rolesManager.saveUserRoles(it)
                        bindUserInfo()
                    }
                    setupChangeRoleButton()
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun setupChangeRoleButton() {
        val canChangeRole = rolesManager.canSwitchRoles()
        profileState = profileState.copy(canChangeRole = canChangeRole)
    }

    private fun bindUserInfo() {
        val userInfo = userManager.getUserInfo()
        profileState = SupervisorProfileUiState(
            displayName = userInfo?.displayName?.takeIf { it.isNotBlank() } ?: userInfo?.userName ?: getString(R.string.supervisor_profile),
            userName = userInfo?.userName,
            mail = userInfo?.mail?.takeIf { it.isNotBlank() },
            roleName = rolesManager.getProfileRoleLabel()
                ?: userInfo?.defaultRoleName?.takeIf { it.isNotBlank() },
            canChangeRole = rolesManager.canSwitchRoles(),
        )
    }

    private fun confirmLogout() {
        showLogoutConfirmation = true
    }

    private fun performLogout() {
        authHelper.logout(this)
    }

    companion object {
        fun open(context: Context) {
            context.startActivity(Intent(context, SupervisorProfileActivity::class.java))
        }
    }
}
