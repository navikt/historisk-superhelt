package no.nav.historisk.superhelt.dokarkiv.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import no.nav.common.types.EksternJournalpostId
import no.nav.common.types.Enhetsnummer
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.dokarkiv.DokarkivService
import no.nav.historisk.superhelt.dokarkiv.JournalforService
import no.nav.historisk.superhelt.dokarkiv.JournalpostService
import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.historisk.superhelt.oppgave.OppgaveService
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.oppgave.OppgaveType
import no.nav.saf.graphql.JournalStatus
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/dokarkiv")
@Tag(name = "Dokarkiv")
class DokarkivJournalforController(
    private val journalpostService: JournalpostService,
    private val dokArkivService: DokarkivService,
    private val oppgaveService: OppgaveService,
    private val journalforService: JournalforService,
    private val sakRepository: SakRepository) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Operation(operationId = "journalfor", summary = "Journalfør en journalpost i dokarkiv")
    @PutMapping("{journalpostId}/journalfor")
    fun journalfor(
        @PathVariable journalpostId: EksternJournalpostId,
        @RequestBody @Valid request: JournalforRequest,
    ): Saksnummer {

        val journalpost =
            journalpostService.hentJournalpost(journalpostId)
                ?: throw IkkeFunnetException("Fant ikke journalpost med id $journalpostId")

        val jfrOppgave = oppgaveService.getOppgave(request.jfrOppgaveId)

        val saksnummer = jfrOppgave.saksnummer ?: journalforService.lagNySakOgKnyttDenTilOppgave(request, jfrOppgave)

        if (journalpost.journalstatus != JournalStatus.JOURNALFOERT) {
            dokArkivService.journalførIArkivet(
                journalPostId = journalpost.journalpostId,
                fagsaksnummer = saksnummer,
                journalfoerendeEnhet = jfrOppgave.tildeltEnhetsnr ?: Enhetsnummer("9999"),
                request = request,
            )
        } else {
            logger.info("Journalpost {} er allerede journalført", journalpostId)
        }

        // Denne er allerede idempotent og vil ikke ferdigstille oppgaven hvis den er ferdigstilt fra før
        oppgaveService.ferdigstillOppgave(request.jfrOppgaveId)

        val sak = sakRepository.getSak(saksnummer)
        oppgaveService.opprettOppgave(
            type = OppgaveType.BEH_SAK,
            sak = sak,
            beskrivelse = "Behandle sak av type ${sak.type.navn} i Superhelt",
            tilordneTil = sak.saksbehandler.navIdent
        )

        return saksnummer


    }


}

