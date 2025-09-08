package no.nav.historisk.superhelt.auth.token

 data class M2MRequest(
    val identity_provider: String = "azuread",
    val target: String
)

 data class OboRequest(
    val identity_provider: String = "azuread",
    val target: String,
    val user_token: String,
)


data class TexasResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int
)