package no.nav.historisk.integration.tilgangsmaskin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

data class ProblemDetaljResponse(
    val type: String,
    val title: Avvisningskoder,
    val status: Int,
    val instance: String,
    val brukerIdent: String,
    val navIdent: String,
    val begrunnelse: String,
    val traceId: String,
    val kanOverstyres: Boolean,
)

sealed interface Avvisningskoder {

    @get:JsonValue
    val value: String


    enum class KjenteVerdier : Avvisningskoder {
        AVVIST_STRENGT_FORTROLIG_ADRESSE,
        AVVIST_STRENGT_FORTROLIG_UTLAND,
        AVVIST_AVDØD,
        AVVIST_PERSON_UTLAND,
        AVVIST_SKJERMING,
        AVVIST_FORTROLIG_ADRESSE,
        AVVIST_PERSON_UKJENT,
        AVVIST_GEOGRAFISK,
        AVVIST_HABILITET,
        ;

        override val value: String
            get() = this.name
    }

    data class Ukjent(override val value: String) : Avvisningskoder

    companion object {
        @JsonCreator
        @JvmStatic
        fun parse(s: String): Avvisningskoder {
            val v = KjenteVerdier.entries.find { it.name == s }
            return v ?: Ukjent(s)
        }
    }
}