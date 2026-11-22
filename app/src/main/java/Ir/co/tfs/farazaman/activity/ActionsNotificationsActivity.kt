package Ir.co.tfs.farazaman.activity

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.data.db.DataHelper
import Ir.co.tfs.farazaman.data.model.AppNotificationDto
import Ir.co.tfs.farazaman.data.repository.AppNotificationRepository
import Ir.co.tfs.farazaman.notification.FirebaseNotificationInboxStore
import Ir.co.tfs.farazaman.notification.NotificationUnreadCountStore
import Ir.co.tfs.farazaman.presentation.compose.*
import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzButtonVariant
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import Ir.co.tfs.farazaman.presentation.viewmodel.ActionsViewModel
import Ir.co.tfs.farazaman.presentation.viewmodel.NotificationsViewModel
import Ir.co.tfs.farazaman.supervisor.actions.*
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ActionsNotificationsActivity : AppCompatActivity(), UserActionOperationHost {
    @Inject lateinit var repository: AppNotificationRepository
    @Inject lateinit var unreadStore: NotificationUnreadCountStore
    private val actionsViewModel: ActionsViewModel by viewModels()
    private val notificationsViewModel: NotificationsViewModel by viewModels()
    private val selectedTab = mutableStateOf(ActionsNotificationsTab.Actions)
    private val actionsState = mutableStateOf(UserActionsUiState())
    private val notificationsState = mutableStateOf(NotificationsUiState())
    private val notificationDetails = mutableStateOf<AppNotificationDto?>(null)
    private val deleteCandidate = mutableStateOf<AppNotificationDto?>(null)
    private var receiverRegistered = false
    private var pendingGpxFile: File? = null

    private val inboxReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) = notificationsViewModel.refresh()
    }
    private val saveGpxLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument(DataHelper.MIME_TYPE_GPX)) { destination ->
        val source = pendingGpxFile.also { pendingGpxFile = null }
        if (destination == null || source == null) return@registerForActivityResult
        lifecycleScope.launch {
            val saved = withContext(Dispatchers.IO) { runCatching { contentResolver.openOutputStream(destination, "w")?.use { out -> source.inputStream().use { it.copyTo(out) } } ?: error("output") }.isSuccess }
            Toast.makeText(this@ActionsNotificationsActivity, if (saved) R.string.action_gpx_saved else R.string.action_gpx_open_failed, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionsViewModel.state.observe(this) { s -> actionsState.value = UserActionsUiState(s.categories, s.items, s.selectedFilter, s.loading, s.hasMore) }
        notificationsViewModel.state.observe(this) { s -> notificationsState.value = NotificationsUiState(s.items, s.loading, s.hasMore, s.error?.message) }
        setContent { FaraZamanTheme {
            ActionsNotificationsScreen(selectedTab.value, ::onShellEvent) { modifier ->
                when (selectedTab.value) {
                    ActionsNotificationsTab.Actions -> UserActionsScreen(actionsState.value, ::onActionsEvent, modifier)
                    ActionsNotificationsTab.Notifications -> NotificationsScreen(notificationsState.value, ::onNotificationsEvent, modifier)
                }
            }
            notificationDetails.value?.let { item ->
                AlertDialog(onDismissRequest = { notificationDetails.value = null }, title = { Text(item.title) },
                    text = { Text(listOfNotNull(item.type, item.createDate, item.link).filter(String::isNotBlank).joinToString("\n")) },
                    confirmButton = { FzButton(getString(R.string.notification_close), { notificationDetails.value = null }) })
            }
            deleteCandidate.value?.let { item ->
                AlertDialog(onDismissRequest = { deleteCandidate.value = null }, text = { Text(getString(R.string.notification_delete_confirm)) },
                    confirmButton = { FzButton(getString(R.string.notification_delete), { deleteCandidate.value = null; notificationsViewModel.delete(item) }) },
                    dismissButton = { FzButton(getString(android.R.string.cancel), { deleteCandidate.value = null }, variant = FzButtonVariant.Ghost) })
            }
        } }
        actionsViewModel.refresh(); notificationsViewModel.refresh()
    }

    override fun onStart() { super.onStart(); ContextCompat.registerReceiver(this, inboxReceiver, IntentFilter(FirebaseNotificationInboxStore.ACTION_INBOX_CHANGED), ContextCompat.RECEIVER_NOT_EXPORTED); receiverRegistered = true }
    override fun onResume() { super.onResume(); actionsViewModel.refresh(); notificationsViewModel.refresh() }
    override fun onStop() { if (receiverRegistered) unregisterReceiver(inboxReceiver); receiverRegistered = false; super.onStop() }

    private fun onShellEvent(e: ActionsNotificationsUiEvent) { when (e) { ActionsNotificationsUiEvent.Back -> finish(); is ActionsNotificationsUiEvent.SelectTab -> selectedTab.value = e.tab } }
    private fun onActionsEvent(e: UserActionsUiEvent) { when(e) { is UserActionsUiEvent.SelectFilter -> actionsViewModel.selectFilter(e.key); is UserActionsUiEvent.RunOperation -> onActionOperation(e.item,e.operation); UserActionsUiEvent.LoadNextPage -> actionsViewModel.loadNextPage() } }
    private fun onNotificationsEvent(e: NotificationsUiEvent) { when(e) { NotificationsUiEvent.Retry -> notificationsViewModel.refresh(); NotificationsUiEvent.LoadNextPage -> notificationsViewModel.loadNextPage(); is NotificationsUiEvent.Open -> openNotification(e.notification); is NotificationsUiEvent.Delete -> deleteCandidate.value=e.notification } }
    private fun openNotification(item: AppNotificationDto) { lifecycleScope.launch { val details=runCatching{repository.details(item.id)}.getOrElse{Toast.makeText(this@ActionsNotificationsActivity,R.string.notification_load_error,Toast.LENGTH_SHORT).show();return@launch};val shown=if(details.isRead)details else runCatching{repository.visit(details.id)}.getOrDefault(details);if(!details.isRead&&shown.isRead){unreadStore.update((unreadStore.current()-1).coerceAtLeast(0));notificationsViewModel.refresh()};notificationDetails.value=shown } }

    override fun onActionOperation(item: UserActionRecord, operationKey: String) { when(operationKey){UserActionOperations.RESUME_MISSION->{val id=item.relatedEntityId?.toLongOrNull()?:return;setResult(Activity.RESULT_OK,Intent().putExtra(EXTRA_RESUME_TRACK_ID,id));finish()};UserActionOperations.OPEN_GPX->openGpx(item)} }
    private fun openGpx(item:UserActionRecord){val file=item.metadata[UserActionMetadataKeys.GPX_PATH]?.let(::File);if(file==null||!file.isFile||file.length()==0L){Toast.makeText(this,R.string.action_gpx_missing,Toast.LENGTH_SHORT).show();return};pendingGpxFile=file;saveGpxLauncher.launch(file.name)}
    companion object { const val EXTRA_RESUME_TRACK_ID="resume_track_id" }
}

interface UserActionOperationHost { fun onActionOperation(item: UserActionRecord, operationKey: String) }
