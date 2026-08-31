package Ir.co.tfs.farazaman.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeveloperOptionsGuardTest {

    @Test
    fun debugBuildWithDeveloperOptionsEnabled_isNotBlocked() {
        assertFalse(
            DeveloperOptionsGuard.shouldBlock(
                isDebugBuild = true,
                developerOptionsEnabled = true,
            ),
        )
    }

    @Test
    fun releaseBuildWithDeveloperOptionsEnabled_isBlocked() {
        assertTrue(
            DeveloperOptionsGuard.shouldBlock(
                isDebugBuild = false,
                developerOptionsEnabled = true,
            ),
        )
    }

    @Test
    fun releaseBuildAfterDeveloperOptionsDisabled_isNotBlocked() {
        assertFalse(
            DeveloperOptionsGuard.shouldBlock(
                isDebugBuild = false,
                developerOptionsEnabled = false,
            ),
        )
    }
}
