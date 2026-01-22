package no.nav.historisk.superhelt.oppgave

import no.nav.common.types.EksternOppgaveId
import no.nav.common.types.NavIdent
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.oppgave.OppgaveClient
import no.nav.oppgave.OppgaveType
import no.nav.oppgave.model.FinnOppgaverParams
import no.nav.oppgave.model.OppgaveDto
import no.nav.oppgave.model.OpprettOppgaveRequest
import no.nav.oppgave.model.PatchOppgaveRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

private const val TEMA_HEL = "HEL"

@Service
class OppgaveService(
    private val oppgaveClient: OppgaveClient,
    private val oppgaveRepository: OppgaveRepository,
    private val sakRepository: SakRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun hentOppgaverForSaksbehandler(navident: NavIdent): List<OppgaveMedSak> {

        val oppgaver = oppgaveClient.finnOppgaver(
            FinnOppgaverParams(
                tilordnetRessurs = navident,
                statuskategori = "AAPEN",
                tema = listOf(TEMA_HEL),
                limit = 50L
            )
        ).oppgaver ?: emptyList()

        return oppgaver.map {
            it.toOppgaveMedSak(
                sak = oppgaveRepository.finnSakForOppgave(it.id)
            )
        }
    }

    @Transactional(readOnly = true)
    fun getOppgave(oppgaveId: EksternOppgaveId): OppgaveMedSak {
        val dto = oppgaveClient.hentOppgave(oppgaveId)
            ?: throw IkkeFunnetException("Fant ikke oppgave med id $oppgaveId")

        val sak = oppgaveRepository.finnSakForOppgave(dto.id)
        return dto.toOppgaveMedSak(sak)
    }

    @Transactional
    fun knyttOppgaveTilSak(saksnummer: Saksnummer, oppgaveId: EksternOppgaveId, oppgaveType: OppgaveType) {
        oppgaveRepository.save(saksnummer, oppgaveId, oppgaveType)
    }

    @Transactional
    fun ferdigstillOppgave(oppgaveId: EksternOppgaveId) {
        val oppgave = oppgaveClient.hentOppgave(oppgaveId)

        if (oppgave == null) {
            logger.warn("Fant ikke oppgave med id {}, kan ikke ferdigstille", oppgaveId)
            return
        }

        if (oppgave.status == OppgaveDto.Status.FERDIGSTILT) {
            logger.debug("Oppgave {} er allerede ferdigstilt, ingen mulighet å oppdatere", oppgave.id)
        }

        oppgaveClient.patchOppgave(
            oppgaveId = oppgaveId,
            request = PatchOppgaveRequest(
                versjon = oppgave.versjon,
                status = OppgaveDto.Status.FERDIGSTILT
            )
        )
        logger.info("Ferdigstiller oppgave med id {}", oppgave.id)
    }

    fun ferdigstillOppgaver(saksnummer: Saksnummer, type: OppgaveType?) {
        val oppgaveIds = oppgaveRepository.finnOppgaverForSak(saksnummer, type)
        oppgaveIds.forEach { oppgaveId ->
            ferdigstillOppgave(oppgaveId)
        }
    }


    @Transactional
    fun opprettOppgave(
        type: OppgaveType,
        saksnummer: Saksnummer,
        tilordneSaksbehandler: Boolean = true): OppgaveMedSak {
        val sak = sakRepository.getSak(saksnummer)
        return opprettOppgave(type, sak, tilordneSaksbehandler)
    }

    @Transactional
    fun opprettOppgave(type: OppgaveType, sak: Sak, tilordneSaksbehandler: Boolean = true): OppgaveMedSak {
        val gjelder = sak.type.tilOppgaveGjelder()
        val oppgave = oppgaveClient.opprettOppgave(
            OpprettOppgaveRequest(
                tema = TEMA_HEL,
                oppgavetype = type.oppgavetype,
//                journalpostId = journalpostId,
                beskrivelse = "Saksbehandling i superhelt",
                personident = sak.fnr.value,
                saksreferanse = sak.saksnummer.value,
                behandlesAvApplikasjon = "SUPERHELT",
                behandlingstype = gjelder.behandlingstype,
                behandlingstema = gjelder.behandlingstema,
                fristFerdigstillelse = LocalDate.now().plusDays(5),
            )
        )

        knyttOppgaveTilSak(sak.saksnummer, oppgave.id, type)

        if (tilordneSaksbehandler) {
            oppgaveClient.patchOppgave(
                oppgaveId = oppgave.id,
                request = PatchOppgaveRequest(
                    versjon = oppgave.versjon,
                    tilordnetRessurs = sak.saksbehandler.navIdent
                )
            )
        }
        logger.info("Oppretter oppgave {}:{} for sak {}", type, oppgave.id, sak.saksnummer)
        return oppgave.toOppgaveMedSak(sak)

    }


}