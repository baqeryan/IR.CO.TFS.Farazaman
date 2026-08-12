package Ir.co.tfs.farazaman.domain.tracking

object TrackingPointPolicy {
    const val WALKING_SPEED_THRESHOLD_MS = 3f
    const val WALKING_MAX_ACCURACY_METERS = 30f
    const val DRIVING_MAX_ACCURACY_METERS = 50f

    @JvmStatic
    fun isAccuracyAcceptable(accuracyMeters: Float, speedMetersPerSecond: Float): Boolean {
        if (!accuracyMeters.isFinite() || accuracyMeters < 0f) return false
        val maximum = if (speedMetersPerSecond < WALKING_SPEED_THRESHOLD_MS) {
            WALKING_MAX_ACCURACY_METERS
        } else {
            DRIVING_MAX_ACCURACY_METERS
        }
        return accuracyMeters <= maximum
    }

    fun shouldLogMovement(
        hasPreviousPoint: Boolean,
        distanceMeters: Float,
        minimumDistanceMeters: Float,
        accuracyMeters: Float,
        speedMetersPerSecond: Float,
    ): Boolean {
        return isAccuracyAcceptable(accuracyMeters, speedMetersPerSecond) &&
            (!hasPreviousPoint || distanceMeters >= minimumDistanceMeters)
    }
}
