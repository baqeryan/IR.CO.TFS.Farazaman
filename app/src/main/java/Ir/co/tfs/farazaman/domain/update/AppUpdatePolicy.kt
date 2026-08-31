package Ir.co.tfs.farazaman.domain.update

import Ir.co.tfs.farazaman.data.model.AppUpdateInfo

sealed interface AppUpdateDecision {
    data object UpToDate : AppUpdateDecision
    data class Optional(val info: AppUpdateInfo) : AppUpdateDecision
    data class Mandatory(val info: AppUpdateInfo) : AppUpdateDecision
}

object AppUpdatePolicy {
    fun decide(installedVersionCode: Long, remote: AppUpdateInfo): AppUpdateDecision {
        if (remote.versionCode <= installedVersionCode) return AppUpdateDecision.UpToDate
        return if (remote.mandatory) {
            AppUpdateDecision.Mandatory(remote)
        } else {
            AppUpdateDecision.Optional(remote)
        }
    }
}
