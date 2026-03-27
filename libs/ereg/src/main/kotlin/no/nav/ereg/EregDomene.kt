package no.nav.ereg

import no.nav.common.types.Organisasjonsnummer

data class EregOrganisasjon(
    val organisasjonsnummer: Organisasjonsnummer,
    val navn: String,
    val postadresse: EregAdresse?,
    val forretningsadresse: EregAdresse?,
)

data class EregAdresse(
    val adresselinje1: String?,
    val adresselinje2: String?,
    val adresselinje3: String?,
    val postnummer: String?,
    val poststed: String?,
    val landkode: String?,
    val land: String?,
)

/** Intern representasjon av EREG-responsens navne-objekt */
internal data class EregNavn(
    val sammensattnavn: String?,
)

/** Intern representasjon av rårespons fra EREG API v2 */
internal data class EregOrganisasjonResponse(
    val organisasjonsnummer: String,
    val navn: EregNavn?,
    val postadresse: EregAdresse?,
    val forretningsadresse: EregAdresse?,
)
