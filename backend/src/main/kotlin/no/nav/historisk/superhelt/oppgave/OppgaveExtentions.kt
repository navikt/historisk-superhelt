package no.nav.historisk.superhelt.oppgave

import no.nav.common.types.FolkeregisterIdent
import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.historisk.superhelt.sak.Sak
import no.nav.oppgave.behandlingstemaEnum
import no.nav.oppgave.behandlingstypeEnum
import no.nav.oppgave.model.OppgaveDto
import no.nav.oppgave.type

val OppgaveDto.gjelderTekst: String
    get() = listOfNotNull(this.behandlingstemaEnum?.term, this.behandlingstypeEnum?.term).joinToString(" ")

fun OppgaveDto.toOppgaveMedSak(sak: Sak?): OppgaveMedSak {
    val ident = this.bruker?.ident ?: sak?.fnr?.value
    ?: throw IkkeFunnetException("Fant ikke personident for oppgave ${this.id}")

    return OppgaveMedSak(
        fnr = FolkeregisterIdent(ident),
        oppgaveId = this.id,
        oppgavestatus = this.status,
        oppgavetype = this.type,
        oppgaveGjelderTekst = this.gjelderTekst,
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
        stonadsType = sak?.type,
        sakBeskrivelse = sak?.beskrivelse,
    )
}


