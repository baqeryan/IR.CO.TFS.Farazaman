package Ir.co.tfs.farazaman.contractor

/**
 * Safe integration boundary for Contractor services whose contracts are not confirmed yet.
 * Implementations must use the selected municipality and authenticated client when supplied.
 */
interface ContractorGateway {
    val isConfigured: Boolean

    fun unavailableMessage(): String
}

object UnconfiguredContractorGateway : ContractorGateway {
    override val isConfigured: Boolean = false

    override fun unavailableMessage(): String =
        "سرویس‌های عملیات پیمانکار هنوز به برنامه متصل نشده‌اند."
}
