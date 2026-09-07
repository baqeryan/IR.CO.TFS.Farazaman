package Ir.co.tfs.farazaman.supervisor

object SupervisorMissionDayPolicy {
    private val isoDatePrefix = Regex("^\\d{4}-\\d{2}-\\d{2}")

    fun requiresRegistration(storedVisitDate: String?, today: String): Boolean {
        val storedDay = storedVisitDate?.trim()
            ?.let { isoDatePrefix.find(it)?.value ?: it }
            .orEmpty()
        return storedDay != today
    }
}
