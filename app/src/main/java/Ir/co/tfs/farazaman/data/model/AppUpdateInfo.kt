package Ir.co.tfs.farazaman.data.model

data class AppUpdateInfo(
    val versionCode: Long,
    val versionName: String,
    val downloadUrl: String,
    val mandatory: Boolean,
    val releaseNotes: String?,
    val publishedAt: String?,
)
