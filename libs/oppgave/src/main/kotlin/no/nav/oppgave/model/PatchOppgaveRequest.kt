package no.nav.oppgave.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class PatchOppgaveRequest(
    val versjon: Int,
    val orgnr: String? = null,
    val status: Status? = null,
    val endretAvEnhetsnr: String? = null,
    val tilordnetRessurs: String? = null,
    val tildeltEnhetsnr: String? = null,
    val prioritet: Prioritet? = null,
    val behandlingstema: String? = null,
    val behandlingstype: String? = null,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val fristFerdigstillelse: LocalDate? = null,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val aktivDato: LocalDate? = null,
    val oppgavetype: String? = null,
    val tema: String? = null,
    val journalpostId: String? = null,
    val saksreferanse: String? = null,
    val behandlesAvApplikasjon: String? = null,
    val personident: String? = null,
    val beskrivelse: String? = null
)