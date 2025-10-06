package no.nav.tilgangsmaskin

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
    // Fra schema https://tilgangsmaskin.intern.nav.no/swagger-ui/index.html#/TilgangController/kjerneregler
    AVVIST_STRENGT_FORTROLIG_ADRESSE,
    AVVIST_STRENGT_FORTROLIG_UTLAND,
    AVVIST_AVDØD,
    AVVIST_PERSON_UTLAND,
    AVVIST_SKJERMING,
    AVVIST_FORTROLIG_ADRESSE,
    AVVIST_UKJENT_BOSTED,
    AVVIST_GEOGRAFISK,
    AVVIST_HABILITET,

    // Egen kode for  person som ikke er funnet
    UKJENT_PERSON
    ;
}


