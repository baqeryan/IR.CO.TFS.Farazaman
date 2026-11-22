package Ir.co.tfs.farazaman.update

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface AppUpdateInstallStatus {
    data object AwaitingUserConfirmation : AppUpdateInstallStatus
    data object Success : AppUpdateInstallStatus
    data class Failure(val message: String) : AppUpdateInstallStatus
}

internal fun isUnknownSourceInstallBlock(message: String): Boolean =
    message.contains("unknown source", ignoreCase = true) ||
        message.contains("unknown sources", ignoreCase = true)

object AppUpdateInstallStatusBus {
    private val mutableEvents = MutableSharedFlow<AppUpdateInstallStatus>(extraBufferCapacity = 8)
    val events = mutableEvents.asSharedFlow()

    fun publish(status: AppUpdateInstallStatus) {
        mutableEvents.tryEmit(status)
    }
}
