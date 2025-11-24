package no.nav.historisk.superhelt.infrastruktur

import jakarta.validation.ConstraintViolationException
import no.nav.historisk.superhelt.infrastruktur.exception.ValidationFieldError
import no.nav.historisk.superhelt.infrastruktur.exception.ValideringException
import no.nav.historisk.superhelt.infrastruktur.exception.createValidationErrorMessage
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.ErrorResponseException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class GlobalControllerAdvice : ResponseEntityExceptionHandler() {
    private val log = LoggerFactory.getLogger(javaClass)

    /** Validation errors in spring method binding */
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<in Any>? {
        val fieldErrors = ex.bindingResult.fieldErrors.map {
            ValidationFieldError(
                field = it.field,
                message = it.defaultMessage ?: "Ukjent valideringsfeil"
            )
        }
        val problemDetail = createValidationErrorMessage(
            title = ex.javaClass.simpleName,
            fieldErrors = fieldErrors
        )
        return super.handleExceptionInternal(ex, problemDetail, headers, HttpStatus.BAD_REQUEST, request)
    }

    /** Validation errors from programmatic validation */
    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationError(ex: ConstraintViolationException): ProblemDetail {
        val detail = ex.message
        log.info("Validation failed. {} Return 400", detail)
        val fieldErrors = ex.constraintViolations.map {
            ValidationFieldError(
                message = it.message,
                field = it.propertyPath.toString(),
            )
        }
        val problemDetail = createValidationErrorMessage(ex.javaClass.simpleName, fieldErrors, detail)
        return problemDetail
    }

    /** Validation errors from programmatic validation */
    @ExceptionHandler(ValideringException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValideringException(ex: ValideringException): ProblemDetail {
        val detail = ex.message
        log.info("Validation failed. {} Return 400", detail)
        val fieldErrors = ex.validationErrors
        val problemDetail = createValidationErrorMessage(ex.javaClass.simpleName, fieldErrors, detail)
        return problemDetail
    }


    override fun handleErrorResponseException(
        ex: ErrorResponseException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest): ResponseEntity<in Any>? {
        log.info("Error: {} returning:  {}", ex.javaClass.simpleName, ex.message)
        return super.handleErrorResponseException(ex, headers, status, request)
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