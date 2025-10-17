package no.nav.historisk.superhelt.auth.exception

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class IkkeFunnetException(
    reason: String?,
    cause: Throwable? = null,
    messageDetailCode: String? = null,
    messageDetailArguments: Array<Any>? = null
) : ResponseStatusException(HttpStatus.NOT_FOUND, reason, cause, messageDetailCode, messageDetailArguments) {
}