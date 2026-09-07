package Ir.co.tfs.farazaman.domain.validation

import android.net.Uri

object ContractorCorrectionValidator {
    enum class Error {
        EMPTY_DESCRIPTION,
        IMAGE_REQUIRED,
    }

    fun validate(description: String, capturedImages: Collection<Uri>): Error? = when {
        description.isBlank() -> Error.EMPTY_DESCRIPTION
        capturedImages.isEmpty() -> Error.IMAGE_REQUIRED
        else -> null
    }
}
