package Ir.co.tfs.farazaman.presentation

import Ir.co.tfs.farazaman.presentation.compose.SurfacePermissionPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SurfacePermissionPolicyTest {
    @Test fun `location accepts either fine or coarse permission`() {
        assertTrue(SurfacePermissionPolicy.canUseLocation(true, false))
        assertTrue(SurfacePermissionPolicy.canUseLocation(false, true))
        assertFalse(SurfacePermissionPolicy.canUseLocation(false, false))
    }

    @Test fun `camera capture requires camera permission`() {
        assertTrue(SurfacePermissionPolicy.canCapture(true))
        assertFalse(SurfacePermissionPolicy.canCapture(false))
    }
}
