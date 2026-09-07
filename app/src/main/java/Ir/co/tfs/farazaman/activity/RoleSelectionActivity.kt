package Ir.co.tfs.farazaman.activity

import android.content.Context
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import Ir.co.tfs.farazaman.presentation.compose.FaraZamanTheme
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.presentation.compose.RoleOption
import Ir.co.tfs.farazaman.presentation.compose.RoleSelectionScreen
import Ir.co.tfs.farazaman.presentation.compose.RoleSelectionUiState
import Ir.co.tfs.farazaman.presentation.compose.RoleSelectionEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import Ir.co.tfs.farazaman.supervisor.SupervisorMissionHelper
import Ir.co.tfs.farazaman.util.RolesManager
import Ir.co.tfs.farazaman.util.AuthHelper
import Ir.co.tfs.farazaman.notification.NotificationUnreadCountStore
import androidx.core.content.ContextCompat
import javax.inject.Inject
import androidx.core.view.WindowInsetsControllerCompat

@AndroidEntryPoint
class RoleSelectionActivity : AppCompatActivity() {

    @Inject lateinit var rolesManager: RolesManager
    @Inject lateinit var authHelper: AuthHelper
    private var unreadCount by mutableStateOf(0)
    private var unreadReceiverRegistered = false

    private val unreadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            unreadCount = intent?.getIntExtra(
                NotificationUnreadCountStore.EXTRA_UNREAD_COUNT,
                0,
            ) ?: 0
        }
    }

    private enum class SelectedRole {
        SUPERVISOR,
        DRIVER,
        CONTRACTOR,
    }

    private data class RoleVisibility(
        val showSupervisor: Boolean,
        val showDriver: Boolean,
        val showContractor: Boolean,
    )

    private val fromProfile: Boolean
        get() = intent.getBooleanExtra(EXTRA_FROM_PROFILE, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.supervisor_header_green)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        unreadCount = NotificationUnreadCountStore.read(this)
        val roleVisibility = roleVisibility()
        val initialRole = when (intent.getStringExtra(EXTRA_CURRENT_ROLE)) {
            ROLE_DRIVER -> if (roleVisibility.showDriver) SelectedRole.DRIVER else roleVisibility.firstAvailable()
            ROLE_CONTRACTOR -> if (roleVisibility.showContractor) SelectedRole.CONTRACTOR else roleVisibility.firstAvailable()
            else -> when {
                roleVisibility.showSupervisor -> SelectedRole.SUPERVISOR
                else -> roleVisibility.firstAvailable()
            }
        }
        setContent {
            FaraZamanTheme {
                var selectedRole by rememberSaveable {
                    mutableStateOf(
                        when (initialRole) {
                            SelectedRole.SUPERVISOR -> RoleOption.SUPERVISOR
                            SelectedRole.DRIVER -> RoleOption.DRIVER
                            SelectedRole.CONTRACTOR -> RoleOption.CONTRACTOR
                        },
                    )
                }
                val state = RoleSelectionUiState(
                    showSupervisor = roleVisibility.showSupervisor,
                    showDriver = roleVisibility.showDriver,
                    showContractor = roleVisibility.showContractor,
                    selectedRole = selectedRole,
                    showBack = fromProfile,
                )
                RoleSelectionScreen(
                    state = state,
                    unreadCount = unreadCount,
                    onEvent = { event ->
                        when (event) {
                            is RoleSelectionEvent.SelectRole -> selectedRole = event.role
                            RoleSelectionEvent.Back -> finish()
                            RoleSelectionEvent.OpenProfile -> SupervisorProfileActivity.open(this)
                            RoleSelectionEvent.OpenNotifications ->
                                startActivity(Intent(this, ActionsNotificationsActivity::class.java))
                            RoleSelectionEvent.Logout -> authHelper.logout(this)
                            RoleSelectionEvent.Enter -> navigateToSelectedRole(
                                when (selectedRole) {
                                    RoleOption.SUPERVISOR -> SelectedRole.SUPERVISOR
                                    RoleOption.DRIVER -> SelectedRole.DRIVER
                                    RoleOption.CONTRACTOR -> SelectedRole.CONTRACTOR
                                },
                            )
                        }
                    },
                )
            }
        }
    }

    private fun roleVisibility(): RoleVisibility {
        val roleResponse = rolesManager.getUserRoles()
        val storedRoles = roleResponse?.roles.orEmpty()
        val storedActorRoles = roleResponse?.actorRoles.orEmpty()
        val access = rolesManager.getUserAccess()
        if (access.isSuperAdmin) {
            return RoleVisibility(
                showSupervisor = true,
                showDriver = true,
                showContractor = true,
            )
        }
        val showSupervisor = rolesManager.hasSupervisorRole()
        val showDriver = rolesManager.hasDriverRole()
        val showContractor = rolesManager.hasContractorRole()

        return when {
            storedRoles.isEmpty() && storedActorRoles.isEmpty() -> RoleVisibility(false, false, false)
            showSupervisor || showDriver || showContractor -> RoleVisibility(
                showSupervisor = showSupervisor,
                showDriver = showDriver,
                showContractor = showContractor,
            )
            storedRoles.size > 1 -> RoleVisibility(
                showSupervisor = true,
                showDriver = true,
                showContractor = false,
            )
            else -> RoleVisibility(false, false, false)
        }
    }

    override fun onStart() {
        super.onStart()
        if (!unreadReceiverRegistered) {
            ContextCompat.registerReceiver(
                this,
                unreadReceiver,
                IntentFilter(NotificationUnreadCountStore.ACTION_UNREAD_COUNT_CHANGED),
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
            unreadReceiverRegistered = true
        }
        unreadCount = NotificationUnreadCountStore.read(this)
    }

    override fun onStop() {
        if (unreadReceiverRegistered) {
            unregisterReceiver(unreadReceiver)
            unreadReceiverRegistered = false
        }
        super.onStop()
    }

    private fun RoleVisibility.firstAvailable(): SelectedRole = when {
        showSupervisor -> SelectedRole.SUPERVISOR
        showDriver -> SelectedRole.DRIVER
        showContractor -> SelectedRole.CONTRACTOR
        else -> SelectedRole.SUPERVISOR
    }

    private fun navigateToSelectedRole(selectedRole: SelectedRole) {
        val intent = when (selectedRole) {
            SelectedRole.SUPERVISOR -> createSupervisorIntent()
            SelectedRole.DRIVER -> Intent(this, DriverActivity::class.java)
            SelectedRole.CONTRACTOR -> Intent(this, ContractorWorkAreaSelectionActivity::class.java)
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
        const val ROLE_CONTRACTOR = "contractor"

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
