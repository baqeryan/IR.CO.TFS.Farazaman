package Ir.co.tfs.farazaman.domain.location

import android.location.Location

sealed interface LocationFix {
    val location: Location

    data class Fresh(override val location: Location) : LocationFix
    data class LastKnown(override val location: Location) : LocationFix
}

object LocationFixPolicy {
    fun select(fresh: Location?, lastKnown: Iterable<Location?>): LocationFix? {
        if (fresh.isUsable()) return LocationFix.Fresh(fresh!!)

        return lastKnown
            .filterNotNull()
            .filter { it.isUsable() }
            .minByOrNull { it.accuracy }
            ?.let(LocationFix::LastKnown)
    }

    fun Location?.isUsable(): Boolean {
        if (this == null) return false
        if (!latitude.isFinite() || !longitude.isFinite()) return false
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return false
        if (latitude == 0.0 && longitude == 0.0) return false
        return hasAccuracy() && accuracy.isFinite() && accuracy > 0f
    }
}
