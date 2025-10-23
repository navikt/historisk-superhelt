package no.nav.historisk.superhelt.infrastruktur

import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class GlobalControllerAdvice : ResponseEntityExceptionHandler() {
    private val log = LoggerFactory.getLogger(javaClass)

    /*Validation errors*/
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<in Any>? {
        val errors = ex.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errors).apply {
            title = ex.javaClass.simpleName
        }
        return super.handleExceptionInternal(ex, problemDetail, headers, HttpStatus.BAD_REQUEST, request)
    }


    @ExceptionHandler(AccessDeniedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleAccessDenied(ex: AccessDeniedException): ProblemDetail {
        log.warn("Access denied. Return 403", ex)
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.message ?: "Access denied")
        problemDetail.title = ex.javaClass.simpleName
        return problemDetail
    }

    @ExceptionHandler(AuthorizationDeniedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleAuthorizationDenied(ex: AuthorizationDeniedException): ProblemDetail {
        val detail: String? = ex.authorizationResult?.toString() ?: ex.message
        log.warn("Access denied. {} Return 403", detail)
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, detail)
        problemDetail.title = ex.javaClass.simpleName
        return problemDetail
    }

    @ExceptionHandler(Throwable::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleAllExceptions(ex: Throwable): ProblemDetail {
        log.error("Exception caught. Return 500", ex)
        val problemDetail =
            ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.message ?: "Unexpected error")
        problemDetail.title = ex.javaClass.simpleName
        return problemDetail
    }
}