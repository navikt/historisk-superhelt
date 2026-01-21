package no.nav.historisk.superhelt.oppgave

import no.nav.common.types.EksternOppgaveId
import no.nav.common.types.FolkeregisterIdent
import no.nav.common.types.NavIdent
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.oppgave.OppgaveClient
import no.nav.oppgave.OppgaveType
import no.nav.oppgave.gjelder
import no.nav.oppgave.model.FinnOppgaverParams
import no.nav.oppgave.model.OppgaveDto
import no.nav.oppgave.model.PatchOppgaveRequest
import no.nav.oppgave.type
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OppgaveService(
    private val oppgaveClient: OppgaveClient,
    private val oppgaveRepository: OppgaveRepository) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun hentOppgaverForSaksbehandler(navident: NavIdent): List<OppgaveMedSak> {

        val oppgaver = oppgaveClient.finnOppgaver(
            FinnOppgaverParams(
                tilordnetRessurs = navident,
                statuskategori = "AAPEN",
                tema = listOf("HEL"),
                limit = 50L
            )
        ).oppgaver ?: emptyList()

        return oppgaver.map { toOppgaveMedSak(it) }
    }
    @Transactional(readOnly = true)
    fun getOppgave(oppgaveId: EksternOppgaveId): OppgaveMedSak {
        val dto = oppgaveClient.hentOppgave(oppgaveId)
            ?: throw IkkeFunnetException("Fant ikke oppgave med id $oppgaveId")

        return toOppgaveMedSak(dto)
    }

    @Transactional
    fun knyttOppgaveTilSak(saksnummer: Saksnummer, oppgaveId: EksternOppgaveId, oppgaveType: OppgaveType) {
        oppgaveRepository.save(saksnummer, oppgaveId, oppgaveType)
    }

    fun ferdigstillOppgave(oppgaveId: EksternOppgaveId) {
        val oppgave = oppgaveClient.hentOppgave(oppgaveId)
            ?: throw IkkeFunnetException("Fant ikke oppgave med id $oppgaveId")

        if (oppgave.status == OppgaveDto.Status.FERDIGSTILT) {
            logger.info("Oppgave {} er allerede ferdigstilt, ingen mulighet å oppdatere", oppgave.id)
        }

        oppgaveClient.patchOppgave(
            oppgaveId = oppgaveId,
            request = PatchOppgaveRequest(
                versjon = oppgave.versjon,
                status = OppgaveDto.Status.FERDIGSTILT
            )
        )
    }

    private fun toOppgaveMedSak(dto: OppgaveDto): OppgaveMedSak {
        val sak = oppgaveRepository.finnSakForOppgave(dto.id)

        val ident = dto.bruker?.ident ?: sak?.fnr?.value
        ?: throw IkkeFunnetException("Fant ikke personident for oppgave ${dto.id}")

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
            opprettetTidspunkt=dto.opprettetTidspunkt,
            behandlesAvApplikasjon = dto.behandlesAvApplikasjon,
            tildeltEnhetsnr = dto.tildeltEnhetsnr,
            opprettetAv = dto.opprettetAv,
            saksnummer = sak?.saksnummer,
            sakStatus = sak?.status,
        )
    }


}