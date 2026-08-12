package Ir.co.tfs.farazaman.domain.validation

object LoginInputValidator {
    fun isValid(username: String, password: String): Boolean {
        return username.isNotBlank() && password.isNotEmpty()
    }
}
