package no.nav.historisk.superhelt.infrastruktur.validation

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import java.net.URI

fun createValidationErrorMessage(
    title: String = "ValidationError",
    fieldErrors: List<ValidationFieldError>,
    detail: String? = fieldErrors.joinToString(", ") { "${it.field}: ${it.message}" }): ProblemDetail {

    val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail)
    problemDetail.type = URI.create("http://nav.no/validation")
    problemDetail.title = title
    problemDetail.setProperty("error", fieldErrors)
    return problemDetail
}

