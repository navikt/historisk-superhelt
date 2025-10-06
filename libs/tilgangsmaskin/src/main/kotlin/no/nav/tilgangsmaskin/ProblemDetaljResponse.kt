package no.nav.tilgangsmaskin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

data class ProblemDetaljResponse(
    val type: String,
    val title: Avvisningskode,
    val status: Int,
    val instance: String,
    val brukerIdent: String,
    val navIdent: String,
    val begrunnelse: String,
    val traceId: String,
    val kanOverstyres: Boolean,
)

enum class Avvisningskode {
    AVVIST_STRENGT_FORTROLIG_ADRESSE,
    AVVIST_STRENGT_FORTROLIG_UTLAND,
    AVVIST_AVDØD,
    AVVIST_PERSON_UTLAND,
    AVVIST_SKJERMING,
    AVVIST_FORTROLIG_ADRESSE,
    AVVIST_PERSON_UKJENT,
    AVVIST_GEOGRAFISK,
    AVVIST_HABILITET;
}


