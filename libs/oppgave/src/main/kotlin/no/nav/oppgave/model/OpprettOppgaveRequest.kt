package no.nav.oppgave.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class OpprettOppgaveRequest(
    val tema: String,
    val oppgavetype: String,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val aktivDato: LocalDate,
    val prioritet: Prioritet,
    val personident: String? = null,
    val orgnr: String? = null,
    val tildeltEnhetsnr: String? = null,
    val opprettetAvEnhetsnr: String? = null,
    val journalpostId: String? = null,
    val behandlesAvApplikasjon: String? = null,
    val beskrivelse: String? = null,
    val behandlingstema: String? = null,
    val behandlingstype: String? = null,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val fristFerdigstillelse: LocalDate? = null
)