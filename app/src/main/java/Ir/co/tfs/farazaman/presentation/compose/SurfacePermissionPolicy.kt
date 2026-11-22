package Ir.co.tfs.farazaman.presentation.compose

/** Centralized permission decisions consumed by routes before camera/location side effects. */
object SurfacePermissionPolicy {
    fun canUseLocation(fineGranted: Boolean, coarseGranted: Boolean): Boolean = fineGranted || coarseGranted
    fun canCapture(cameraGranted: Boolean): Boolean = cameraGranted
}
