package no.nav.historisk.superhelt.oppgave

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.common.types.*
import no.nav.historisk.superhelt.person.MaskertPersonIdent
import no.nav.historisk.superhelt.person.toMaskertPersonIdent
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.oppgave.OppgaveGjelderTemaHel
import no.nav.oppgave.OppgaveTypeTemaHel
import no.nav.oppgave.model.OppgaveDto
import java.time.LocalDate

data class OppgaveMedSak(
    val fnr: FolkeregisterIdent,
    val oppgaveId: EksternOppgaveId,
    val oppgavestatus: OppgaveDto.Status,
    val oppgavetype: OppgaveTypeTemaHel,
    val oppgaveGjelder: OppgaveGjelderTemaHel,
    val journalpostId: EksternJournalpostId?,
    val tilordnetRessurs: NavIdent?,
    val beskrivelse: String?,
    val fristFerdigstillelse: LocalDate?,
    val behandlesAvApplikasjon: String?,
    val tildeltEnhetsnr: String?,

    val saksnummer: Saksnummer?,
    val sakStatus: SakStatus?,
){
    @get:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val maskertPersonIdent: MaskertPersonIdent
        get() = fnr.toMaskertPersonIdent()
}