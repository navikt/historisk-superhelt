package no.nav.historisk.superhelt.dokarkiv.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import no.nav.common.types.EksternJournalpostId
import no.nav.common.types.Saksnummer
import no.nav.common.types.defaultEnhetsnummer
import no.nav.historisk.superhelt.dokarkiv.DokarkivService
import no.nav.historisk.superhelt.dokarkiv.JournalforService
import no.nav.historisk.superhelt.dokarkiv.JournalpostService
import no.nav.historisk.superhelt.endringslogg.EndringsloggService
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
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
    private val sakRepository: SakRepository,
    private val endringsloggService: EndringsloggService) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Operation(operationId = "journalforNySak", summary = "Journalfør en journalpost og knytter til ny sak")
    @PutMapping("{journalpostId}/journalfor/ny")
    fun journalforNysak(
        @PathVariable journalpostId: EksternJournalpostId,
        @RequestBody @Valid request: JournalforRequest,
    ): Saksnummer {

        val journalpost =
            journalpostService.hentJournalpost(journalpostId)
                ?: throw IkkeFunnetException("Fant ikke journalpost med id $journalpostId")

        val jfrOppgave = oppgaveService.getOppgave(request.jfrOppgaveId)

        // Henter opprettet sak eller lager en ny
        val saksnummer = jfrOppgave.saksnummer ?: journalforService.lagNySakOgKnyttDenTilOppgave(request, jfrOppgave)

        if (journalpost.journalstatus != JournalStatus.JOURNALFOERT) {
            dokArkivService.journalførIArkivet(
                journalPostId = journalpost.journalpostId,
                fagsaksnummer = saksnummer,
                journalfoerendeEnhet = jfrOppgave.tildeltEnhetsnr ?: defaultEnhetsnummer,
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
            tilordneTil = sak.saksbehandler.navIdent,
            journalpostId = journalpostId
        )

        return saksnummer
    }

    @Operation(
        operationId = "journalforKnyttTilEksisterendeSak",
        summary = "Journalfør og knytt til en eksisternde sak"
    )
    @PutMapping("{journalpostId}/journalfor/eksisterende")
    fun journalforKnyttTilEksisterendeSak(
        @PathVariable journalpostId: EksternJournalpostId,
        @RequestBody @Valid request: JournalforEksisterendeSakRequest,
    ): Saksnummer {

        val journalpost =
            journalpostService.hentJournalpost(journalpostId)
                ?: throw IkkeFunnetException("Fant ikke journalpost med id $journalpostId")

        val jfrOppgave = oppgaveService.getOppgave(request.jfrOppgaveId)
        val saksnummer = request.saksnummer
        val sak = sakRepository.getSak(saksnummer)

        oppgaveService.knyttOppgaveTilSak(
            saksnummer = saksnummer,
            oppgaveId = request.jfrOppgaveId,
            oppgaveType = OppgaveType.JFR
        )

        if (journalpost.journalstatus != JournalStatus.JOURNALFOERT) {
            dokArkivService.journalførIArkivet(
                journalPostId = journalpost.journalpostId,
                fagsaksnummer = saksnummer,
                journalfoerendeEnhet = jfrOppgave.tildeltEnhetsnr ?: defaultEnhetsnummer,
                request = request,
            )
            endringsloggService.logChange(
                saksnummer = saksnummer,
                endringsType = EndringsloggType.DOKUMENT_JOURNALFOERT_EKSISTERENDE_SAK,
                endring = "Dokument journalført",
                beskrivelse = "Dokument av type \"${jfrOppgave.oppgaveGjelder.stringValue}\" er journalført på saken"
            )
        } else {
            logger.info("Journalpost {} er allerede journalført", journalpostId)
        }

        // Denne er allerede idempotent og vil ikke ferdigstille oppgaven hvis den er ferdigstilt fra før
        oppgaveService.ferdigstillOppgave(request.jfrOppgaveId)
        // TODO sjekk om det finnes åpne oppgaver for denne saken. Trenger vi en til?
        oppgaveService.opprettOppgave(
            type = OppgaveType.VUR,
            sak = sak,
            beskrivelse = "Dokument av  ${jfrOppgave.oppgaveGjelder.stringValue} er lagt til sak ${sak.saksnummer} i Superhelt. " +
                    "Vurder videre behandling av saken. Lukk denne oppgaven om det ikke skal gjøres noe spesiell oppfølging.",
            tilordneTil = sak.saksbehandler.navIdent,
            journalpostId = journalpostId
        )

        return saksnummer
    }


}

