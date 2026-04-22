package no.nav.historisk.superhelt.oppgave

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.common.types.EksternJournalpostId
import no.nav.common.types.EksternOppgaveId
import no.nav.common.types.Enhetsnummer
import no.nav.common.types.FolkeregisterIdent
import no.nav.common.types.NavIdent
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.StonadsType
import no.nav.historisk.superhelt.person.MaskertPersonIdent
import no.nav.historisk.superhelt.person.toMaskertPersonIdent
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.oppgave.OppgaveGjelder
import no.nav.oppgave.OppgaveType
import no.nav.oppgave.model.OppgaveDto
import java.time.LocalDate
import java.time.OffsetDateTime

data class OppgaveMedSak(
    val fnr: FolkeregisterIdent,
    val oppgaveId: EksternOppgaveId,
    val oppgavestatus: OppgaveDto.Status,
    val oppgavetype: OppgaveType,
    val oppgaveGjelder: OppgaveGjelder,
    val journalpostId: EksternJournalpostId?,
    val tilordnetRessurs: NavIdent?,
    val beskrivelse: String?,
    val fristFerdigstillelse: LocalDate?,
    val opprettetTidspunkt: OffsetDateTime?,
    val behandlesAvApplikasjon: String?,
    val tildeltEnhetsnr: Enhetsnummer?,
    val opprettetAv: String?,

    val saksnummer: Saksnummer?,
    val sakStatus: SakStatus?,
    val stonadsType: StonadsType?,
    val sakBeskrivelse: String?

){
    @get:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val maskertPersonIdent: MaskertPersonIdent
        get() = fnr.toMaskertPersonIdent()
}
