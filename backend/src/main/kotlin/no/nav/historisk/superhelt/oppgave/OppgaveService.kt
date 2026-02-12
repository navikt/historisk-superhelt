package no.nav.historisk.superhelt.oppgave

import no.nav.common.types.EksternOppgaveId
import no.nav.common.types.NavIdent
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.historisk.superhelt.sak.Sak
import no.nav.oppgave.OppgaveClient
import no.nav.oppgave.OppgaveType
import no.nav.oppgave.model.FinnOppgaverParams
import no.nav.oppgave.model.OppgaveDto
import no.nav.oppgave.model.OpprettOppgaveRequest
import no.nav.oppgave.model.PatchOppgaveRequest
import no.nav.oppgave.type
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

private const val TEMA_HEL = "HEL"

@Service
class OppgaveService(
    private val oppgaveClient: OppgaveClient,
    private val oppgaveRepository: OppgaveRepository,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @PreAuthorize("hasAuthority('READ')")
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

    @PreAuthorize("hasAuthority('READ')")
    @Transactional(readOnly = true)
    fun getOppgave(oppgaveId: EksternOppgaveId): OppgaveMedSak {
        val dto = oppgaveClient.hentOppgave(oppgaveId)
            ?: throw IkkeFunnetException("Fant ikke oppgave med id $oppgaveId")

        val sak = oppgaveRepository.finnSakForOppgave(dto.id)
        return dto.toOppgaveMedSak(sak)
    }

    @PreAuthorize("hasAuthority('WRITE')")
    @Transactional
    fun knyttOppgaveTilSak(saksnummer: Saksnummer, oppgaveId: EksternOppgaveId, oppgaveType: OppgaveType) {
        oppgaveRepository.save(saksnummer, oppgaveId, oppgaveType)
    }

    @PreAuthorize("hasAuthority('WRITE')")
    fun ferdigstillOppgave(oppgaveId: EksternOppgaveId) {
        val oppgave = oppgaveClient.hentOppgave(oppgaveId)

        if (oppgave == null) {
            logger.warn("Fant ikke oppgave med id {}, kan ikke ferdigstille", oppgaveId)
            return
        }

        if (oppgave.status == OppgaveDto.Status.FERDIGSTILT) {
            logger.debug("Oppgave {} er allerede ferdigstilt, ingen mulighet å oppdatere", oppgave.id)
            return
        }

        oppgaveClient.patchOppgave(
            oppgaveId = oppgaveId,
            request = PatchOppgaveRequest(
                versjon = oppgave.versjon,
                status = OppgaveDto.Status.FERDIGSTILT
            )
        )
        logger.info("Ferdigstiller oppgave {}: {}", oppgave.type, oppgave.id)
    }

    /** Ferdigstiller alle oppgaver av gitt type for en sak. Hvis ingen type er oppgitt, ferdigstilles alle oppgaver for saken. */
    @PreAuthorize("hasAuthority('WRITE')")
    fun ferdigstillOppgaver(saksnummer: Saksnummer, vararg types: OppgaveType?) {
        if (types.isEmpty()) {
            logger.debug("Ferdigstiller alle oppgaver for sak {}", saksnummer)
            val oppgaveIds = oppgaveRepository.finnOppgaverForSak(saksnummer, null)
            oppgaveIds.forEach { oppgaveId ->
                ferdigstillOppgave(oppgaveId)
            }
        } else {
            logger.debug("Ferdigstiller oppgaver for sak {} av type {}", saksnummer, types.joinToString(","))
            types.forEach { type ->
                val oppgaveIds = oppgaveRepository.finnOppgaverForSak(saksnummer, type)
                oppgaveIds.forEach { oppgaveId ->
                    ferdigstillOppgave(oppgaveId)
                }
            }
        }
    }

    @PreAuthorize("hasAuthority('WRITE') and @tilgangsmaskin.harTilgang(#sak.fnr)")
    @Transactional
    fun opprettOppgave(
        type: OppgaveType, sak: Sak,
        beskrivelse: String? = null,
        tilordneTil: NavIdent? = null): OppgaveMedSak {
        val gjelder = sak.type.tilOppgaveGjelder()
        val oppgave = oppgaveClient.opprettOppgave(
            OpprettOppgaveRequest(
                tema = TEMA_HEL,
                oppgavetype = type.oppgavetype,
//                journalpostId = journalpostId,
                beskrivelse = beskrivelse,
                personident = sak.fnr.value,
                saksreferanse = sak.saksnummer.value,
                behandlesAvApplikasjon = "SUPERHELT",
                behandlingstype = gjelder.behandlingstype,
                behandlingstema = gjelder.behandlingstema,
                fristFerdigstillelse = LocalDate.now().plusDays(5),
            )
        )

        knyttOppgaveTilSak(sak.saksnummer, oppgave.id, type)

        if (tilordneTil != null) {
            oppgaveClient.patchOppgave(
                oppgaveId = oppgave.id,
                request = PatchOppgaveRequest(
                    versjon = oppgave.versjon,
                    tilordnetRessurs = tilordneTil
                )
            )
        }
        logger.info("Oppretter oppgave {}:{} for sak {}", type, oppgave.id, sak.saksnummer)
        return oppgave.toOppgaveMedSak(sak)

    }
}