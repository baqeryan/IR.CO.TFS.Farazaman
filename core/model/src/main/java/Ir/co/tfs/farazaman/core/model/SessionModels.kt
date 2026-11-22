package Ir.co.tfs.farazaman.core.model

data class Municipality(
    val id: String,
    val title: String,
    val baseUrl: String,
    val loginMode: LoginMode,
    val oidcClientId: String? = null,
    val oidcScope: String? = null,
)

enum class LoginMode {
    OIDC,
    PASSWORD,
}

data class UserIdentity(
    val id: String,
    val displayName: String,
)

data class UserRole(
    val key: String,
    val displayName: String,
)

data class SessionSnapshot(
    val municipality: Municipality,
    val user: UserIdentity?,
    val roles: Set<UserRole>,
    val isAuthenticated: Boolean,
)
