package Ir.co.tfs.farazaman.contractor

object ContractorDraftValidator {
    enum class Error { DESCRIPTION_REQUIRED, IMAGE_REQUIRED }

    fun validate(description: String, imageCount: Int): Error? = when {
        description.isBlank() -> Error.DESCRIPTION_REQUIRED
        imageCount < 1 -> Error.IMAGE_REQUIRED
        else -> null
    }
}
