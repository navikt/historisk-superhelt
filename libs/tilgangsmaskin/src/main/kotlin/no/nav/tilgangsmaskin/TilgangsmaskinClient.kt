package no.nav.tilgangsmaskin

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient

class TilgangsmaskinClient(private val restClient: RestClient) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun komplett(personident: String): TilgangResult {
        return sjekkTilgang("/api/v1/komplett", personident)
    }

    fun kjerne(personident: String): TilgangResult {
        return sjekkTilgang("/api/v1/kjerne", personident)
    }

    private fun sjekkTilgang(url: String, personident: String): TilgangResult {
        try {
            val responseEntity = restClient
                .post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(personident)
                .retrieve()
                // Forventer 403 ved avvisning
                .onStatus({ it == HttpStatus.FORBIDDEN }) { _, _ -> }
                .toEntity(ProblemDetaljResponse::class.java)

            return when {
                responseEntity.statusCode.is2xxSuccessful -> TilgangResult(true)
                responseEntity.statusCode.isSameCodeAs(HttpStatus.FORBIDDEN) -> TilgangResult(
                    false,
                    responseEntity.body
                )

                else -> throw RuntimeException("Uventet respons fra tilgangsmaskin: ${responseEntity.statusCode}")
            }
        } catch (ex: HttpClientErrorException.NotFound) {
            log.info("Person $personident ikke funnet i tilgangsmaskin", ex)
            return TilgangResult(
                false, ProblemDetaljResponse(
                    type = "https://nav.no/tilgangskontroll/ikke-funnet",
                    title = Avvisningskode.UKJENT_PERSON,
                    status = HttpStatus.NOT_FOUND.value(),
                    instance = "",
                    brukerIdent = personident,
                    navIdent = "",
                    begrunnelse = "Personen finnes ikke",
                    traceId = "",
                    kanOverstyres = false
                )
            )

        }
    }

    data class TilgangResult(val harTilgang: Boolean, val response: ProblemDetaljResponse? = null)
}
