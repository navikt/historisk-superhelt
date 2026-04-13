package no.nav.historisk.superhelt.samhandler

import no.nav.common.types.Organisasjonsnummer
import no.nav.ereg.EregAdresse
import no.nav.ereg.EregClient
import org.springframework.stereotype.Service

//Om vi velger å bruke BrReg isteden kan det endres her.
@Service
class SamhandlerService(private val eregClient: EregClient) {

    fun hentSamhandler(orgnr: Organisasjonsnummer): Samhandler? {
        val organisasjon = eregClient.hentOrganisasjon(orgnr) ?: return null
        return Samhandler(
            organisasjonsnummer = organisasjon.organisasjonsnummer,
            navn = organisasjon.navn,
            postadresse = organisasjon.postadresse?.toAdresse(),
            forretningsadresse = organisasjon.forretningsadresse?.toAdresse(),
        )
    }
}

private fun EregAdresse.toAdresse() = Adresse(
    adresselinje1 = adresselinje1,
    adresselinje2 = adresselinje2,
    adresselinje3 = adresselinje3,
    postnummer = postnummer,
    poststed = poststed,
    landkode = landkode,
    land = land,
)
