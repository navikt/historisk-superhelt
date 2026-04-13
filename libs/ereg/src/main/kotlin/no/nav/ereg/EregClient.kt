package no.nav.ereg

import no.nav.common.types.Organisasjonsnummer
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

/**
 * Klient mot EREG (Enhetsregisteret) API v2.
 * Dokumentasjon: https://ereg-services.intern.nav.no/swagger-ui/index.html
 */
class EregClient(private val restClient: RestClient) {

    /**
     * Slår opp organisasjon med tilhørende adresse.
     * Returnerer null dersom organisasjonsnummeret ikke finnes (404).
     */
    fun hentOrganisasjon(orgnr: Organisasjonsnummer): EregOrganisasjon? {
        val response = restClient.get()
            .uri("/api/v2/organisasjon/{orgnr}", orgnr.value)
            .retrieve()
            .onStatus({ it.value() == 404 }) { _, _ -> }
            .body<EregOrganisasjonResponse>()
            ?: return null

        return EregOrganisasjon(
            organisasjonsnummer = Organisasjonsnummer(response.organisasjonsnummer),
            navn = response.navn?.sammensattnavn ?: "",
            postadresse = response.postadresse,
            forretningsadresse = response.forretningsadresse,
        )
    }
}
