package no.nav.historisk.superhelt.oppgave

import no.nav.common.types.FolkeregisterIdent
import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.StonadsType
import no.nav.oppgave.OppgaveGjelder
import no.nav.oppgave.gjelder
import no.nav.oppgave.model.OppgaveDto
import no.nav.oppgave.type

fun StonadsType.tilOppgaveGjelder(): OppgaveGjelder =
    when (this) {
        StonadsType.PARYKK -> OppgaveGjelder.PARYKK_HODEPLAGG
        StonadsType.PROTESE, StonadsType.SPESIALSKO,
        StonadsType.ORTOSE, StonadsType.FOTSENG -> OppgaveGjelder.ORTOPEDISKE_HJELPEMIDLER
        StonadsType.ANSIKT_PROTESE -> OppgaveGjelder.ANSIKTSDEFEKTSPROTESE
        StonadsType.OYE_PROTESE -> OppgaveGjelder.OYEPROTESE
        StonadsType.BRYSTPROTESE -> OppgaveGjelder.BRYSTPROTESE_PROTESEBH
        StonadsType.FOTTOY -> OppgaveGjelder.ORTOPEDISKE_HJELPEMIDLER
        StonadsType.REISEUTGIFTER -> OppgaveGjelder.REISEUTGIFTER
    }

fun OppgaveDto.toOppgaveMedSak(sak: Sak?): OppgaveMedSak {
    val ident = this.bruker?.ident ?: sak?.fnr?.value
    ?: throw IkkeFunnetException("Fant ikke personident for oppgave ${this.id}")

    return OppgaveMedSak(
        fnr = FolkeregisterIdent(ident),
        oppgaveId = this.id,
        oppgavestatus = this.status,
        oppgavetype = this.type,
        oppgaveGjelder = this.gjelder,
        journalpostId = this.journalpostId,
        tilordnetRessurs = this.tilordnetRessurs,
        beskrivelse = this.beskrivelse,
        fristFerdigstillelse = this.fristFerdigstillelse,
        opprettetTidspunkt = this.opprettetTidspunkt,
        behandlesAvApplikasjon = this.behandlesAvApplikasjon,
        tildeltEnhetsnr = this.tildeltEnhetsnr,
        opprettetAv = this.opprettetAv,
        saksnummer = sak?.saksnummer,
        sakStatus = sak?.status,
    )
}
