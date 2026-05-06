package no.nav.historisk.superhelt.oppgave

import no.nav.common.types.FolkeregisterIdent
import no.nav.historisk.superhelt.StonadsType
import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.historisk.superhelt.sak.Sak
import no.nav.oppgave.Behandlingstema.ANSIKTSDEFEKTSPROTESE
import no.nav.oppgave.Behandlingstema.ARBEIDS_OG_UTDANNINGSREISER
import no.nav.oppgave.Behandlingstema.BRYSTPROTESE_PROTESEBH
import no.nav.oppgave.Behandlingstema.FORNYELSESSOKNAD_ORTOPEDISKE_HJELPEMIDLER
import no.nav.oppgave.Behandlingstema.ORTOPEDISKE_HJELPEMIDLER
import no.nav.oppgave.Behandlingstema.OYEPROTESE
import no.nav.oppgave.Behandlingstema.PARYKK_HODEPLAGG
import no.nav.oppgave.Behandlingstema.REISEPENGER_UTPROVING_ORT_TEKNISKE_HJELPEMIDLER
import no.nav.oppgave.Behandlingstema.REISEUTGIFTER
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
        stonadsType = sak?.type?: this.guessStonadsType(),
        sakBeskrivelse = sak?.beskrivelse,
    )
}

/** Tipper hva slags stønad dette gjelder for */
private fun OppgaveDto.guessStonadsType(): StonadsType? {
    //TODO har tema noe å si?
    return when (this.behandlingstemaEnum){
        ORTOPEDISKE_HJELPEMIDLER,FORNYELSESSOKNAD_ORTOPEDISKE_HJELPEMIDLER -> StonadsType.FOTSENG
        ANSIKTSDEFEKTSPROTESE -> StonadsType.ANSIKT_PROTESE
        BRYSTPROTESE_PROTESEBH -> StonadsType.BRYSTPROTESE
        OYEPROTESE -> StonadsType.OYE_PROTESE
        PARYKK_HODEPLAGG -> StonadsType.PARYKK
        REISEPENGER_UTPROVING_ORT_TEKNISKE_HJELPEMIDLER -> StonadsType.REISEUTGIFTER
        REISEUTGIFTER -> StonadsType.REISEUTGIFTER // Litt usikker på denne

        ARBEIDS_OG_UTDANNINGSREISER -> StonadsType.ARBEID_UTDANNING
        else -> null
    }

}


