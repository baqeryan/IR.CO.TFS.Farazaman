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

enum class ApkValidationFailure {
    EMPTY_FILE,
    INVALID_ARCHIVE,
    PACKAGE_MISMATCH,
    VERSION_MISMATCH,
    SIGNATURE_UNAVAILABLE,
    SIGNATURE_MISMATCH,
}

class ApkValidationException(
    val failure: ApkValidationFailure,
    message: String,
) : IllegalStateException(message)

class AndroidApkFileValidator @Inject constructor(
    @ApplicationContext private val context: Context,
) : ApkFileValidator {
    override fun validate(file: File, expected: AppUpdateInfo) {
        validate(file.isFile && file.length() > 0, ApkValidationFailure.EMPTY_FILE) {
            "Downloaded APK is empty"
        }
        validate(hasZipHeader(file), ApkValidationFailure.INVALID_ARCHIVE) {
            "Downloaded file is not an APK"
        }

        val packageManager = context.packageManager
        val archive = packageInfoForArchive(packageManager, file)
            ?: throw ApkValidationException(
                ApkValidationFailure.INVALID_ARCHIVE,
                "Android could not parse the downloaded APK",
            )
        validate(archive.packageName == context.packageName, ApkValidationFailure.PACKAGE_MISMATCH) {
            "APK package name does not match"
        }
        validate(versionCode(archive) == expected.versionCode, ApkValidationFailure.VERSION_MISMATCH) {
            "APK versionCode does not match metadata"
        }

        val installed = packageInfoForInstalled(packageManager)
        val installedCertificates = signingCertificates(installed)
        val archiveCertificates = signingCertificates(archive)
        validate(
            installedCertificates.current.isNotEmpty() && archiveCertificates.current.isNotEmpty(),
            ApkValidationFailure.SIGNATURE_UNAVAILABLE,
        ) {
            "Android could not read APK signing certificates"
        }
        validate(
            ApkSigningCertificatePolicy.isCompatible(installedCertificates, archiveCertificates),
            ApkValidationFailure.SIGNATURE_MISMATCH,
        ) {
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
        val flags = PackageManager.GET_SIGNING_CERTIFICATES or PackageManager.GET_SIGNATURES
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageArchiveInfo(
                file.absolutePath,
                PackageManager.PackageInfoFlags.of(flags.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageArchiveInfo(
                file.absolutePath,
                flags,
            )
        }
    }

    private fun packageInfoForInstalled(packageManager: PackageManager): PackageInfo {
        val flags = PackageManager.GET_SIGNING_CERTIFICATES or PackageManager.GET_SIGNATURES
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(flags.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(
                context.packageName,
                flags,
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

    private fun signingCertificates(info: PackageInfo): ApkSigningCertificates {
        @Suppress("DEPRECATION")
        val legacy = digestSignatures(info.signatures ?: emptyArray())

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return ApkSigningCertificates(current = legacy, history = legacy)
        }

        val signingInfo = info.signingInfo
            ?: return ApkSigningCertificates(current = legacy, history = legacy)
        val current = digestSignatures(signingInfo.apkContentsSigners ?: emptyArray())
            .ifEmpty { legacy }
        val history = if (signingInfo.hasMultipleSigners()) {
            current
        } else {
            digestSignatures(signingInfo.signingCertificateHistory ?: emptyArray())
                .ifEmpty { current }
        }
        return ApkSigningCertificates(current = current, history = history)
    }

    private fun digestSignatures(signatures: Array<Signature>): Set<String> {
        return signatures.mapTo(mutableSetOf()) { signature ->
            MessageDigest.getInstance("SHA-256")
                .digest(signature.toByteArray())
                .joinToString("") { byte -> "%02x".format(byte) }
        }
    }

    private inline fun validate(
        condition: Boolean,
        failure: ApkValidationFailure,
        lazyMessage: () -> String,
    ) {
        if (!condition) throw ApkValidationException(failure, lazyMessage())
    }
}

internal data class ApkSigningCertificates(
    val current: Set<String>,
    val history: Set<String>,
)

internal object ApkSigningCertificatePolicy {
    fun isCompatible(
        installed: ApkSigningCertificates,
        archive: ApkSigningCertificates,
    ): Boolean {
        if (installed.current.isEmpty() || archive.current.isEmpty()) return false

        // The common case is the same current signer. If Play-style certificate rotation is
        // introduced later, the archive history must still contain the currently installed key.
        return installed.current.any(archive.current::contains) ||
            installed.current.any(archive.history::contains)
    }
}
