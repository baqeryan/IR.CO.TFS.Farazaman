package Ir.co.tfs.farazaman.core.common

/** A transport- and platform-neutral result for domain boundaries. */
sealed interface Outcome<out T> {
    data class Success<T>(val value: T) : Outcome<T>
    data class Failure(val error: AppFailure) : Outcome<Nothing>
}

sealed interface AppFailure {
    val cause: Throwable?

    data class Network(override val cause: Throwable? = null) : AppFailure
    data class Unauthorized(override val cause: Throwable? = null) : AppFailure
    data class Validation(
        val reason: String,
        override val cause: Throwable? = null,
    ) : AppFailure
    data class Storage(override val cause: Throwable? = null) : AppFailure
    data class Unexpected(override val cause: Throwable? = null) : AppFailure
}

inline fun <T, R> Outcome<T>.map(transform: (T) -> R): Outcome<R> = when (this) {
    is Outcome.Success -> Outcome.Success(transform(value))
    is Outcome.Failure -> this
}
