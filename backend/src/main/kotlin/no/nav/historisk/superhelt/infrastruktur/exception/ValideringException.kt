package no.nav.historisk.superhelt.infrastruktur.exception

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class ValideringException(
    reason: String?,
    cause: Throwable? = null,
    messageDetailCode: String? = "valideringsfeil",
    messageDetailArguments: Array<Any>? = null
) : ResponseStatusException(HttpStatus.BAD_REQUEST, reason, cause, messageDetailCode, messageDetailArguments) {
}