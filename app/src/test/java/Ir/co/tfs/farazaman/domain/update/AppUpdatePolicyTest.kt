package Ir.co.tfs.farazaman.domain.update

import Ir.co.tfs.farazaman.data.model.AppUpdateInfo
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdatePolicyTest {
    @Test
    fun `equal or older remote version does not require update`() {
        assertSame(AppUpdateDecision.UpToDate, AppUpdatePolicy.decide(65, update(versionCode = 65)))
        assertSame(AppUpdateDecision.UpToDate, AppUpdatePolicy.decide(65, update(versionCode = 64)))
    }

    @Test
    fun `newer optional version produces optional decision`() {
        assertTrue(AppUpdatePolicy.decide(65, update(versionCode = 66)) is AppUpdateDecision.Optional)
    }

    @Test
    fun `newer mandatory version produces mandatory decision`() {
        assertTrue(
            AppUpdatePolicy.decide(65, update(versionCode = 66, mandatory = true))
                is AppUpdateDecision.Mandatory,
        )
    }

    private fun update(versionCode: Long, mandatory: Boolean = false) = AppUpdateInfo(
        versionCode = versionCode,
        versionName = "1.2.3",
        downloadUrl = "https://ftp.tfs.co.ir/farazaman.apk",
        mandatory = mandatory,
        releaseNotes = null,
        publishedAt = null,
    )
}
