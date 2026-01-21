package no.nav.historisk.mock.oppgave

import no.nav.common.types.EksternOppgaveId
import no.nav.common.types.NavIdent
import no.nav.historisk.mock.pdl.fnrFromAktoerId
import no.nav.oppgave.OppgaveType
import no.nav.oppgave.model.OppgaveDto
import no.nav.oppgave.model.OpprettOppgaveRequest
import no.nav.oppgave.model.PatchOppgaveRequest
import no.nav.oppgave.model.SokOppgaverResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("oppgave-mock")
class OppgaveMockController() {

    private val repository = mutableMapOf<EksternOppgaveId, OppgaveDto>()
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        generateTestdata(OppgaveType.JFR, 10000)
    }

    private fun generateTestdata(type: OppgaveType, id: Long) {
        val oppgave = generateOppgave(fnr = "11111111111", tilordnetRessurs = NavIdent(defaultSaksbehandler) ).copy(
            oppgavetype = type.oppgavetype,
            id = EksternOppgaveId(id),
        )
        repository.put(oppgave.id, oppgave)
    }

    @GetMapping()
    fun info(): MutableMap<EksternOppgaveId, OppgaveDto> {
        return repository
    }


    @GetMapping("/api/v1/oppgaver")
    fun finnOppgaver(@RequestParam tilordnetRessurs: String?, @RequestParam aktoerId: String?): SokOppgaverResponse {
        val aktiveOppgaver = repository.values
            .filter { it.status != OppgaveDto.Status.FERDIGSTILT }
            .filter {
                tilordnetRessurs?.equals(it.tilordnetRessurs?.value, ignoreCase = true) ?: true
            }
            .filter {
                aktoerId?.equals(it.aktoerId?.value, ignoreCase = true) ?: true
            }
            .toMutableList()

        // Lager en default jfr oppgave om det er tomt
        val jfrOppgaver = aktiveOppgaver
            .filter { it.oppgavetype == OppgaveType.JFR.oppgavetype }
        if (jfrOppgaver.isEmpty()) {
            val fnr = aktoerId?.let { fnrFromAktoerId(it) }
            val oppgave = generateOppgave(fnr = fnr, tilordnetRessurs = NavIdent( tilordnetRessurs?: defaultSaksbehandler)).copy(
                oppgavetype = OppgaveType.JFR.oppgavetype,
            )
            aktiveOppgaver.add(oppgave)
            repository[oppgave.id] = oppgave
        }

        return SokOppgaverResponse(antallTreffTotalt = aktiveOppgaver.size.toLong(), oppgaver = aktiveOppgaver)
    }

    @GetMapping("/api/v1/oppgaver/{id}")
    fun hentOppgave(@PathVariable("id") id: EksternOppgaveId): ResponseEntity<OppgaveDto> {
        val oppgave = repository[id] ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(oppgave)
    }

    @PostMapping("/api/v1/oppgaver")
    fun opprettOppgave(@RequestBody request: OpprettOppgaveRequest): OppgaveDto {
        logger.info("Oppretter oppgave {}", request)
        val aktoerId = request.personident?: throw IllegalStateException("Fant ikke personident i oppgaver")
        val fnr= fnrFromAktoerId(aktoerId)
        val nyOppgave = generateOppgave(fnr = fnr).copy(
            journalpostId = request.journalpostId,
            tema = request.tema,
            oppgavetype = request.oppgavetype,
            beskrivelse = request.beskrivelse,
            behandlingstema = request.behandlingstema,
            behandlingstype = request.behandlingstype,
        )
        repository.put(nyOppgave.id, nyOppgave)
        // Steng tilhørende journalføringsoppgave hvis dn er koblet samme journalpostid
        // Simulering av at det gjøres i gosys
        repository.values
            .filter { it.oppgavetype == OppgaveType.JFR.oppgavetype }
            .filter { it.journalpostId == nyOppgave.journalpostId }
            .forEach { repository[it.id] = it.copy(status = OppgaveDto.Status.FERDIGSTILT) }


        return nyOppgave
    }

    @PatchMapping("/api/v1/oppgaver/{id}")
    fun patchOppgave(@PathVariable id: EksternOppgaveId, @RequestBody request: PatchOppgaveRequest): ResponseEntity<OppgaveDto> {
        logger.info("Patch oppgave {} {}", id, request)
        val oppgave = repository[id] ?: return ResponseEntity.notFound().build()

        val patchOppgaveGjelder= !request.behandlingstema.isNullOrBlank()  || !request.behandlingstype.isNullOrBlank()
        val patchedOppgave = oppgave.copy(
            versjon = request.versjon,
            status = request.status?.let { OppgaveDto.Status.valueOf(it.name) } ?: oppgave.status,
            tildeltEnhetsnr = request.tildeltEnhetsnr ?: oppgave.tildeltEnhetsnr,
            tilordnetRessurs = request.tilordnetRessurs ?: oppgave.tilordnetRessurs,
            fristFerdigstillelse = request.fristFerdigstillelse ?: oppgave.fristFerdigstillelse,
            journalpostId = request.journalpostId ?: oppgave.journalpostId,
            tema = request.tema ?: oppgave.tema,
            oppgavetype = request.oppgavetype ?: oppgave.oppgavetype,
            behandlingstema = if(patchOppgaveGjelder) request.behandlingstema else oppgave.behandlingstema,
            behandlingstype = if(patchOppgaveGjelder) request.behandlingstype else oppgave.behandlingstype,
        )
        repository[patchedOppgave.id] = patchedOppgave

        return ResponseEntity.ok(patchedOppgave)
    }
}