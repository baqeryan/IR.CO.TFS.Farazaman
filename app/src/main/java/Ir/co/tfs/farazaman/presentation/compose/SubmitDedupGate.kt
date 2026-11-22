package Ir.co.tfs.farazaman.presentation.compose

/** Small, thread-safe gate used by routes to prevent double taps from creating two submissions. */
class SubmitDedupGate {
    @Volatile private var inFlight = false
    fun tryAcquire(): Boolean = synchronized(this) { if (inFlight) false else { inFlight = true; true } }
    fun release() = synchronized(this) { inFlight = false }
    val isInFlight: Boolean get() = inFlight
}
