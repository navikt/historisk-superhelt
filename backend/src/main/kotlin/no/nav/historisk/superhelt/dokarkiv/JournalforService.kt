package no.nav.historisk.superhelt.dokarkiv

import no.nav.common.types.Aar
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.dokarkiv.rest.JournalforRequest
import no.nav.historisk.superhelt.infrastruktur.getCurrentNavUser
import no.nav.historisk.superhelt.oppgave.OppgaveMedSak
import no.nav.historisk.superhelt.oppgave.OppgaveService
import no.nav.historisk.superhelt.sak.OpprettSakDto
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.UpdateSakDto
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class JournalforService (
    private val oppgaveService: OppgaveService,
    private val sakRepository: SakRepository,
){



    @PreAuthorize("hasAuthority('WRITE') and @tilgangsmaskin.harTilgang(#request.bruker)")
    @Transactional
     fun lagNySakOgKnyttDenTilOppgave(
        request: JournalforRequest,
        jfrOppgave: OppgaveMedSak): Saksnummer {
        val soknadsDato = jfrOppgave.opprettetTidspunkt
        val nySak = sakRepository.opprettNySak(
            req = OpprettSakDto(
                type = request.stonadsType,
                fnr = request.bruker,
                properties = UpdateSakDto(
                    soknadsDato = soknadsDato?.toLocalDate(),
                    tildelingsAar = soknadsDato?.let { Aar(it.year) },
                    saksbehandler = getCurrentNavUser(),
                    //TODO lagre journalpostid i sak

                    )
            )
        )
        oppgaveService.knyttOppgaveTilSak(
            saksnummer = nySak.saksnummer,
            oppgaveId = jfrOppgave.oppgaveId,
            oppgaveType = jfrOppgave.oppgavetype
        )
        return nySak.saksnummer
    }
}