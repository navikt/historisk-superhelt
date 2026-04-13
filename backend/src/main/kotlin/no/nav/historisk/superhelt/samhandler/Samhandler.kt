package no.nav.historisk.superhelt.samhandler

import no.nav.common.types.Organisasjonsnummer

data class Samhandler(
    val organisasjonsnummer: Organisasjonsnummer,
    val navn: String,
    val postadresse: Adresse?,
    val forretningsadresse: Adresse?,
)

data class Adresse(
    val adresselinje1: String?,
    val adresselinje2: String?,
    val adresselinje3: String?,
    val postnummer: String?,
    val poststed: String?,
    val landkode: String?,
    val land: String?,
)
