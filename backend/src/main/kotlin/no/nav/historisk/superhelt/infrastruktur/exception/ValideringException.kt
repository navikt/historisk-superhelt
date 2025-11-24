package no.nav.historisk.superhelt.infrastruktur.exception

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class ValideringException(
    reason: String?,
    cause: Throwable? = null,
    messageDetailCode: String? = "Valideringsfeil",
    messageDetailArguments: Array<Any>? = null,
    val validationErrors: List<ValidationFieldError>) :
    ResponseStatusException(HttpStatus.BAD_REQUEST, reason, cause, messageDetailCode, messageDetailArguments) {


    override val message: String
        get() = "${reason ?: "Valideringsfeil"}: ${validationErrors.joinToString(", ") { "${it.field}: ${it.message}" }}"
       
}