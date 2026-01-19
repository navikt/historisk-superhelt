package no.nav.historisk.superhelt.oppgave

import no.nav.common.types.FolkeregisterIdent
import no.nav.common.types.NavIdent
import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.oppgave.OppgaveClient
import no.nav.oppgave.gjelder
import no.nav.oppgave.model.FinnOppgaverParams
import no.nav.oppgave.model.OppgaveDto
import no.nav.oppgave.type
import org.springframework.stereotype.Service

@Service
class OppgaveService(
    private val oppgaveClient: OppgaveClient,
    private val oppgaveRepository: OppgaveRepository) {

    fun hentOppgaverForSaksbehandler(navident: NavIdent): List<OppgaveMedSak> {

        val oppgaver = oppgaveClient.finnOppgaver(
            FinnOppgaverParams(
                tilordnetRessurs = navident,
                statuskategori = "AAPEN",
                tema = listOf("HEL")
            )
        ).oppgaver ?: emptyList()

        return oppgaver.map { toOppgaveMedSak(it) }

    }

    private fun toOppgaveMedSak(dto: OppgaveDto): OppgaveMedSak {
        val sak = oppgaveRepository.finnSakForOppgave(dto.id)

        val ident= dto.bruker?.ident ?: sak?.fnr?.value ?: throw IkkeFunnetException("Fant ikke personident for oppgave ${dto.id}")

        return OppgaveMedSak(
            fnr = FolkeregisterIdent(ident),
            oppgaveId = dto.id,
            oppgavestatus = dto.status,
            oppgavetype = dto.type,
            oppgaveGjelder = dto.gjelder,
            journalpostId = dto.journalpostId,
            tilordnetRessurs = dto.tilordnetRessurs,
            beskrivelse = dto.beskrivelse,
            fristFerdigstillelse = dto.fristFerdigstillelse,
            behandlesAvApplikasjon = dto.behandlesAvApplikasjon,
            tildeltEnhetsnr = dto.tildeltEnhetsnr,
            saksnummer = sak?.saksnummer,
            sakStatus = sak?.status
        )
    }
}