package no.nav.historisk.superhelt.dokarkiv

import no.nav.common.types.Aar
import no.nav.common.types.NavIdent
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.dokarkiv.rest.JournalforRequest
import no.nav.historisk.superhelt.endringslogg.EndringsloggService
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.infrastruktur.authentication.getAuthenticatedUser
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
    private val endringsloggService: EndringsloggService
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
                    saksbehandler = getAuthenticatedUser().navUser,
                    //TODO lagre journalpostid i sak
                    )
            )
        )
        soknadsDato?.let {
                endringsloggService.logChange(
                    saksnummer = nySak.saksnummer,
                    endringsType = EndringsloggType.DOKUMENT_MOTTATT,
                    endring = "Dokument mottatt av NAV",
                    tidspunkt = it.toInstant(),
                    navBruker = NavIdent("system"),
                    beskrivelse = "Dokument av type \"${jfrOppgave.oppgaveGjelder.stringValue}\" registrert som mottatt"
                )
        }
        endringsloggService.logChange(
            saksnummer = nySak.saksnummer,
            endringsType = EndringsloggType.OPPRETTET_SAK,
            endring = "Sak opprettet"
        )

        oppgaveService.knyttOppgaveTilSak(
            saksnummer = nySak.saksnummer,
            oppgaveId = jfrOppgave.oppgaveId,
            oppgaveType = jfrOppgave.oppgavetype
        )
        return nySak.saksnummer
    }
}