package Ir.co.tfs.farazaman.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateInstallStatusTest {
    @Test
    fun `unknown source self-update failure requires security settings`() {
        assertTrue(
            isUnknownSourceInstallBlock(
                "INSTALL_FAILED_ABORTED: Self update is blocked by unknown source package",
            ),
        )
    }

    @Test
    fun `unrelated install failure remains retryable`() {
        assertFalse(isUnknownSourceInstallBlock("INSTALL_FAILED_VERSION_DOWNGRADE"))
    }
}
