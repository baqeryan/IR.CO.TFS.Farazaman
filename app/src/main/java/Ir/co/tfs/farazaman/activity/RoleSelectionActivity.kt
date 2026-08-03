package Ir.co.tfs.farazaman.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import Ir.co.tfs.farazaman.presentation.compose.FaraZamanTheme
import Ir.co.tfs.farazaman.presentation.compose.RoleOption
import Ir.co.tfs.farazaman.presentation.compose.RoleSelectionScreen
import Ir.co.tfs.farazaman.presentation.compose.RoleSelectionUiState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import Ir.co.tfs.farazaman.supervisor.SupervisorMissionHelper
import Ir.co.tfs.farazaman.util.RolesManager
import javax.inject.Inject

@AndroidEntryPoint
class RoleSelectionActivity : AppCompatActivity() {

    @Inject lateinit var rolesManager: RolesManager

    private enum class SelectedRole {
        SUPERVISOR,
        DRIVER,
    }

    private val fromProfile: Boolean
        get() = intent.getBooleanExtra(EXTRA_FROM_PROFILE, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val roleVisibility = roleVisibility()
        val initialRole = when (intent.getStringExtra(EXTRA_CURRENT_ROLE)) {
            ROLE_DRIVER -> SelectedRole.DRIVER
            else -> when {
                roleVisibility.first -> SelectedRole.SUPERVISOR
                roleVisibility.second -> SelectedRole.DRIVER
                else -> SelectedRole.SUPERVISOR
            }
        }
        setContent {
            FaraZamanTheme {
                var selectedRole by rememberSaveable {
                    mutableStateOf(
                        if (initialRole == SelectedRole.SUPERVISOR) {
                            RoleOption.SUPERVISOR
                        } else {
                            RoleOption.DRIVER
                        },
                    )
                }
                val state = RoleSelectionUiState(
                    showSupervisor = roleVisibility.first,
                    showDriver = roleVisibility.second,
                    selectedRole = selectedRole,
                    showBack = fromProfile,
                )
                RoleSelectionScreen(
                    state = state,
                    onRoleSelected = { selectedRole = it },
                    onBack = ::finish,
                    onProfile = { SupervisorProfileActivity.open(this) },
                    onEnter = {
                        navigateToSelectedRole(
                            if (selectedRole == RoleOption.SUPERVISOR) {
                                SelectedRole.SUPERVISOR
                            } else {
                                SelectedRole.DRIVER
                            },
                        )
                    },
                )
            }
        }
    }

    private fun roleVisibility(): Pair<Boolean, Boolean> {
        val storedRoles = rolesManager.getUserRoles()?.roles
        val showSupervisor = rolesManager.hasSupervisorRole()
        val showDriver = rolesManager.hasDriverRole()

        return when {
            storedRoles.isNullOrEmpty() -> true to true
            showSupervisor && showDriver -> true to true
            showSupervisor -> true to false
            showDriver -> false to true
            storedRoles.size > 1 -> true to true
            else -> false to false
        }
    }

    private fun navigateToSelectedRole(selectedRole: SelectedRole) {
        val intent = when (selectedRole) {
            SelectedRole.SUPERVISOR -> createSupervisorIntent()
            SelectedRole.DRIVER -> Intent(this, DriverActivity::class.java)
        }
        if (fromProfile) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        finish()
    }

    private fun createSupervisorIntent(): Intent {
        val target = if (SupervisorMissionHelper.hasExistingMission(this)) {
            Ir.co.tfs.farazaman.activity.NewSupervisorActivity::class.java
        } else {
            SupervisorWorkAreaSelectionActivity::class.java
        }
        return Intent(this, target)
    }

    companion object {
        const val EXTRA_FROM_PROFILE = "from_profile"
        const val EXTRA_CURRENT_ROLE = "current_role"
        const val ROLE_SUPERVISOR = "supervisor"
        const val ROLE_DRIVER = "driver"

        fun createIntent(
            context: Context,
            fromProfile: Boolean = false,
            currentRole: String = ROLE_SUPERVISOR,
        ): Intent = Intent(context, RoleSelectionActivity::class.java).apply {
            putExtra(EXTRA_FROM_PROFILE, fromProfile)
            putExtra(EXTRA_CURRENT_ROLE, currentRole)
        }
    }
}
