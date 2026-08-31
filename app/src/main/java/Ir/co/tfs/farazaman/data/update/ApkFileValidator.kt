package Ir.co.tfs.farazaman.data.update

import Ir.co.tfs.farazaman.data.model.AppUpdateInfo
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import javax.inject.Inject

interface ApkFileValidator {
    fun validate(file: File, expected: AppUpdateInfo)
}

class AndroidApkFileValidator @Inject constructor(
    @ApplicationContext private val context: Context,
) : ApkFileValidator {
    override fun validate(file: File, expected: AppUpdateInfo) {
        require(file.isFile && file.length() > 0) { "Downloaded APK is empty" }
        require(hasZipHeader(file)) { "Downloaded file is not an APK" }

        val packageManager = context.packageManager
        val archive = packageInfoForArchive(packageManager, file)
            ?: error("Android could not parse the downloaded APK")
        require(archive.packageName == context.packageName) { "APK package name does not match" }
        require(versionCode(archive) == expected.versionCode) { "APK versionCode does not match metadata" }

        val installed = packageInfoForInstalled(packageManager)
        val installedCertificates = certificateDigests(installed)
        val archiveCertificates = certificateDigests(archive)
        require(installedCertificates.isNotEmpty() && installedCertificates == archiveCertificates) {
            "APK signing certificate does not match the installed application"
        }
    }

    private fun hasZipHeader(file: File): Boolean {
        val header = ByteArray(4)
        val count = FileInputStream(file).use { it.read(header) }
        return count == 4 && header[0] == 0x50.toByte() && header[1] == 0x4B.toByte() &&
            header[2] == 0x03.toByte() && header[3] == 0x04.toByte()
    }

    private fun packageInfoForArchive(packageManager: PackageManager, file: File): PackageInfo? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageArchiveInfo(
                file.absolutePath,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageArchiveInfo(
                file.absolutePath,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    PackageManager.GET_SIGNING_CERTIFICATES
                } else {
                    PackageManager.GET_SIGNATURES
                },
            )
        }
    }

    private fun packageInfoForInstalled(packageManager: PackageManager): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(
                context.packageName,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    PackageManager.GET_SIGNING_CERTIFICATES
                } else {
                    PackageManager.GET_SIGNATURES
                },
            )
        }
    }

    private fun versionCode(info: PackageInfo): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong()
        }
    }

    private fun certificateDigests(info: PackageInfo): Set<String> {
        val signatures: Array<Signature> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val signingInfo = info.signingInfo ?: return emptySet()
            if (signingInfo.hasMultipleSigners()) {
                signingInfo.apkContentsSigners
            } else {
                signingInfo.signingCertificateHistory
            }
        } else {
            @Suppress("DEPRECATION")
            info.signatures ?: return emptySet()
        }
        return signatures.mapTo(mutableSetOf()) { signature ->
            MessageDigest.getInstance("SHA-256")
                .digest(signature.toByteArray())
                .joinToString("") { byte -> "%02x".format(byte) }
        }
    }
}
