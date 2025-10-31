package no.nav.historisk.superhelt.test

import org.assertj.core.api.AbstractObjectAssert
import org.springframework.http.ProblemDetail
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert

fun MvcTestResultAssert.bodyAsProblemDetail(): AbstractObjectAssert<*, ProblemDetail?> {
    return this.bodyJson().convertTo(ProblemDetail::class.java)
}