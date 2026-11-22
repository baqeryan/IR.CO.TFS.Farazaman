package Ir.co.tfs.farazaman.domain.validation

object ViolationFormValidator {
    enum class Error { INVALID_COUNT, EMPTY_ADDRESS }

    fun validateBasic(number: Int, address: String): Error? = when {
        number <= 0 -> Error.INVALID_COUNT
        address.isBlank() -> Error.EMPTY_ADDRESS
        else -> null
    }
}
