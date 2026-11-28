package com.karasu256.karasulab.ui.debug

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import java.security.MessageDigest

data class SigningFingerprints(val sha1: String, val sha256: String)

fun signingFingerprints(context: Context): SigningFingerprints? {
    val signatures = signingCertificates(context) ?: return null
    val certificate = signatures.firstOrNull()?.toByteArray() ?: return null
    return SigningFingerprints(sha1 = digest(certificate, "SHA-1"), sha256 = digest(certificate, "SHA-256"))
}

private fun signingCertificates(context: Context): Array<Signature>? {
    val packageManager = context.packageManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val info = packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        info.signingInfo?.apkContentsSigners
    } else {
        @Suppress("DEPRECATION")
        val info = packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
        @Suppress("DEPRECATION")
        info.signatures
    }
}

private fun digest(certificate: ByteArray, algorithm: String): String =
    MessageDigest.getInstance(algorithm).digest(certificate).joinToString(":") { "%02X".format(it) }
