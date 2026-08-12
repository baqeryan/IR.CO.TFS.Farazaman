package Ir.co.tfs.farazaman.domain.validation

object ViolationFormValidator {
    enum class Error { INVALID_COUNT, EMPTY_ADDRESS, EMPTY_DESCRIPTION }

    fun validateBasic(number: Int, address: String, description: String): Error? = when {
        number <= 0 -> Error.INVALID_COUNT
        address.isBlank() -> Error.EMPTY_ADDRESS
        description.isBlank() -> Error.EMPTY_DESCRIPTION
        else -> null
    }
}
