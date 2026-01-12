package no.nav.oppgave.model

import java.time.LocalDate
import java.time.OffsetDateTime

data class Oppgave(
    val id: Long,
    val tildeltEnhetsnr: String,
    val tema: String,
    val oppgavetype: String,
    val versjon: Int,
    val prioritet: Prioritet,
    val status: Status,
    val aktivDato: LocalDate,
    val personident: String? = null,
    val endretAvEnhetsnr: String? = null,
    val opprettetAvEnhetsnr: String? = null,
    val journalpostId: String? = null,
    val behandlesAvApplikasjon: String? = null,
    val saksreferanse: String? = null,
    val aktoerId: String? = null,
    val orgnr: String? = null,
    val tilordnetRessurs: String? = null,
    val beskrivelse: String? = null,
    val behandlingstema: String? = null,
    val behandlingstype: String? = null,
    val mappeId: Long? = null,
    val opprettetAv: String? = null,
    val endretAv: String? = null,
    val fristFerdigstillelse: LocalDate? = null,
    val opprettetTidspunkt: OffsetDateTime? = null,
    val ferdigstiltTidspunkt: OffsetDateTime? = null,
    val endretTidspunkt: OffsetDateTime? = null,
    val bruker: Bruker? = null
) {
    data class Bruker (
        val ident : String,
        val type : BrukerType

        )

    enum class BrukerType {
        PERSON,
        ARBEIDSGIVER,
        SAMHANDLER

    }
}