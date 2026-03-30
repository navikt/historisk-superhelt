package no.nav.kabal

/**
 * Kastes når kommunikasjon med Kabal API feiler.
 * @param statusCode HTTP-statuskode fra Kabal, eller null ved nettverksfeil
 * @param responseBody Svarteksten fra Kabal, hvis tilgjengelig
 */
class KabalException(
    message: String,
    cause: Throwable? = null,
    val statusCode: Int? = null,
    val responseBody: String? = null,
) : RuntimeException(message, cause)

