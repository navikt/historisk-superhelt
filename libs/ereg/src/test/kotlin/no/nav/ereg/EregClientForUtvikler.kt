package no.nav.ereg

import no.nav.common.types.Organisasjonsnummer
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestClient

/**
 * Manuell test for å verifisere at EREG returnerer gyldig adresse i dev-miljøet.
 * Kjøres manuelt — ikke en del av automatisk testsuiten.
 *
 * Generer token med client_id api://dev-fss.ereg.ereg-services/.default via Nais Texas.
 */
@Disabled
class EregClientForUtvikler {

    private val accessToken = """
        <lim inn token her>
    """.trimIndent()

    private val baseUrl = "https://ereg-services.intern.dev.nav.no"

    private val eregClient = EregClient(
        RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Authorization", "Bearer ${accessToken.trim()}")
            .build()
    )

    @Test
    fun `slå opp organisasjon i dev`() {
        val orgnr = Organisasjonsnummer("889640782") // NAV Arbeids- og velferdsetaten

        val organisasjon = eregClient.hentOrganisasjon(orgnr)

        println("Organisasjon: $organisasjon")
        println("Postadresse: ${organisasjon?.postadresse}")
    }

    @Test
    fun `slå opp ukjent orgnr returnerer null`() {
        val orgnr = Organisasjonsnummer("000000000")

        val organisasjon = eregClient.hentOrganisasjon(orgnr)

        println("Resultat for ukjent orgnr: $organisasjon")
        check(organisasjon == null) { "Forventet null for ukjent orgnr, fikk: $organisasjon" }
    }
}
