package Ir.co.tfs.farazaman.system

import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import Ir.co.tfs.farazaman.activity.TrackDetailComposeActivity
import Ir.co.tfs.farazaman.activity.TrackManager
import Ir.co.tfs.farazaman.activity.TrackManagerComposeActivity
import Ir.co.tfs.farazaman.activity.WaypointListComposeActivity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManifestNavigationAuditTest {
    private val context get() = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test fun composeMigrationActivitiesAreInternal() {
        val pm = context.packageManager
        listOf(TrackManagerComposeActivity::class.java, TrackDetailComposeActivity::class.java, WaypointListComposeActivity::class.java).forEach { activity ->
            assertFalse(pm.getActivityInfo(ComponentName(context, activity), PackageManager.ComponentInfoFlags.of(0)).exported)
        }
    }

    @Test fun legacyTrackEntryRemainsExplicitlyReachable() {
        val info = context.packageManager.getActivityInfo(ComponentName(context, TrackManager::class.java), PackageManager.ComponentInfoFlags.of(0))
        assertTrue(info.enabled)
    }
}
