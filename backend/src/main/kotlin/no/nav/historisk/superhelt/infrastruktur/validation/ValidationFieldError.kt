package no.nav.historisk.superhelt.infrastruktur.validation

data class ValidationFieldError(
    val field: String,
    val message: String
)