package no.nav.historisk.mock.ereg

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class EregNavn(val sammensattnavn: String)

data class EregAdresse(
    val adresselinje1: String?,
    val adresselinje2: String?,
    val adresselinje3: String?,
    val postnummer: String?,
    val poststed: String?,
    val landkode: String?,
    val land: String?,
)

data class EregOrganisasjonResponse(
    val organisasjonsnummer: String,
    val navn: EregNavn?,
    val postadresse: EregAdresse?,
    val forretningsadresse: EregAdresse?,
)

@RestController
@RequestMapping("ereg-mock")
class EregMockController {

    private val organisasjoner = mapOf(
        "998765432" to EregOrganisasjonResponse(
            organisasjonsnummer = "998765432",
            navn = EregNavn("Norsk Ortopedi AS"),
            forretningsadresse = EregAdresse(
                adresselinje1 = "Storgata 1",
                adresselinje2 = null,
                adresselinje3 = null,
                postnummer = "0182",
                poststed = "OSLO",
                landkode = "NO",
                land = "NORGE",
            ),
            postadresse = null,
        ),
        "987654321" to EregOrganisasjonResponse(
            organisasjonsnummer = "987654321",
            navn = EregNavn("Apotek 1 Gruppen AS"),
            forretningsadresse = EregAdresse(
                adresselinje1 = "Drammensveien 852",
                adresselinje2 = null,
                adresselinje3 = null,
                postnummer = "1372",
                poststed = "ASKER",
                landkode = "NO",
                land = "NORGE",
            ),
            postadresse = EregAdresse(
                adresselinje1 = "Postboks 100",
                adresselinje2 = null,
                adresselinje3 = null,
                postnummer = "1371",
                poststed = "ASKER",
                landkode = "NO",
                land = "NORGE",
            ),
        ),
        "974761084" to EregOrganisasjonResponse(
            organisasjonsnummer = "974761084",
            navn = EregNavn("NAV"),
            forretningsadresse = EregAdresse(
                adresselinje1 = "Fyrstikkalléen 1",
                adresselinje2 = null,
                adresselinje3 = null,
                postnummer = "0661",
                poststed = "OSLO",
                landkode = "NO",
                land = "NORGE",
            ),
            postadresse = null,
        ),
    )

    @GetMapping("/api/v2/organisasjon/{orgnr}")
    fun hentOrganisasjon(@PathVariable orgnr: String): ResponseEntity<EregOrganisasjonResponse> {
        val organisasjon = organisasjoner[orgnr] ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(organisasjon)
    }
}
