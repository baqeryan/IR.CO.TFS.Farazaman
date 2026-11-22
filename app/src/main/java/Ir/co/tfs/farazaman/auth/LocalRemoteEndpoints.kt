package Ir.co.tfs.farazaman.auth

import java.net.URI

object LocalRemoteEndpoints {
    fun fromBaseAddress(baseAddress: String): Endpoints {
        val normalized = baseAddress.trim().trimEnd('/')
        val baseUri = URI(normalized)
        require(baseUri.scheme.equals("https", ignoreCase = true)) {
            "LocalRemote requires an HTTPS BaseAddress"
        }
        require(!baseUri.host.isNullOrBlank()) {
            "LocalRemote BaseAddress must contain a host"
        }
        require(baseUri.query == null && baseUri.fragment == null) {
            "LocalRemote BaseAddress must not contain a query or fragment"
        }

        return Endpoints(
            authorizationEndpoint = URI("$normalized/Authorize"),
            tokenEndpoint = URI("$normalized/api/token"),
        )
    }

    data class Endpoints(
        val authorizationEndpoint: URI,
        val tokenEndpoint: URI,
    )
}
