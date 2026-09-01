package Ir.co.tfs.farazaman.data.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApkSigningCertificatePolicyTest {
    @Test
    fun `same current signing certificate is accepted`() {
        assertTrue(
            ApkSigningCertificatePolicy.isCompatible(
                installed = certificates(current = setOf("release")),
                archive = certificates(current = setOf("release")),
            ),
        )
    }

    @Test
    fun `valid forward certificate rotation is accepted`() {
        assertTrue(
            ApkSigningCertificatePolicy.isCompatible(
                installed = certificates(current = setOf("old-release")),
                archive = certificates(
                    current = setOf("new-release"),
                    history = setOf("old-release", "new-release"),
                ),
            ),
        )
    }

    @Test
    fun `different signing certificates are rejected`() {
        assertFalse(
            ApkSigningCertificatePolicy.isCompatible(
                installed = certificates(current = setOf("old-key")),
                archive = certificates(current = setOf("new-key")),
            ),
        )
    }

    @Test
    fun `missing certificate information is rejected`() {
        assertFalse(
            ApkSigningCertificatePolicy.isCompatible(
                installed = certificates(current = emptySet()),
                archive = certificates(current = setOf("release")),
            ),
        )
    }

    private fun certificates(
        current: Set<String>,
        history: Set<String> = current,
    ) = ApkSigningCertificates(current = current, history = history)
}
