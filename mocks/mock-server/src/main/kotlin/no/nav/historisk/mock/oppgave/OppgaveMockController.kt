package no.nav.historisk.mock.oppgave

import no.nav.historisk.mock.pdl.fnrFromAktoerId
import no.nav.oppgave.OppgaveTypeTemaHel
import no.nav.oppgave.models.Oppgave
import no.nav.oppgave.models.OpprettOppgaveRequest
import no.nav.oppgave.models.PatchOppgaveRequest
import no.nav.oppgave.models.SokOppgaverResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("oppgave-mock")
class OppgaveMockController() {

    private val repository = mutableMapOf<Long, Oppgave>()
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        generateTestdata(OppgaveTypeTemaHel.JFR, 10000)
    }

    private fun generateTestdata(type: OppgaveTypeTemaHel, id: Long) {
        val oppgave = generateOppgave(fnr = "11111111111", tilordnetRessurs = defaultSaksbehandler ).copy(
            oppgavetype = type.oppgavetype,
            id = id,
        )
        repository.put(oppgave.id, oppgave)
    }

    @GetMapping()
    fun info(): MutableMap<Long, Oppgave> {
        return repository
    }


    @GetMapping("/api/v1/oppgaver")
    fun finnOppgaver(@RequestParam tilordnetRessurs: String?, @RequestParam aktoerId: String?): SokOppgaverResponse {
        val aktiveOppgaver = repository.values
            .filter { it.status != Oppgave.Status.FERDIGSTILT }
            .filter {
                if (tilordnetRessurs != null) {
                    it.tilordnetRessurs.equals(tilordnetRessurs, ignoreCase = true)
                } else {
                    true
                }
            }
            .filter {
                if (aktoerId != null) {
                    it.aktoerId.equals(aktoerId, ignoreCase = true)
                } else {
                    true
                }
            }
            .toMutableList()

        // Lager en default jfr oppgave om det er tomt
        val jfrOppgaver = aktiveOppgaver
            .filter { it.oppgavetype == OppgaveTypeTemaHel.JFR.oppgavetype }
        if (jfrOppgaver.isEmpty()) {
            val fnr = aktoerId?.let { fnrFromAktoerId(it) }
            val oppgave = generateOppgave(fnr = fnr, tilordnetRessurs = tilordnetRessurs?: defaultSaksbehandler).copy(
                oppgavetype = OppgaveTypeTemaHel.JFR.oppgavetype,
            )
            aktiveOppgaver.add(oppgave)
            repository.put(oppgave.id, oppgave)
        }

        return SokOppgaverResponse(antallTreffTotalt = aktiveOppgaver.size.toLong(), oppgaver = aktiveOppgaver)
    }

    @GetMapping("/api/v1/oppgaver/{id}")
    fun hentOppgave(@PathVariable("id") id: Long): ResponseEntity<Oppgave> {
        val oppgave = repository.get(id)
        if (oppgave == null) return ResponseEntity.notFound().build()
        return ResponseEntity.ok(oppgave)
    }

    @PostMapping("/api/v1/oppgaver")
    fun opprettOppgave(@RequestBody request: OpprettOppgaveRequest): Oppgave {
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
            .filter { it.oppgavetype == OppgaveTypeTemaHel.JFR.oppgavetype }
            .filter { it.journalpostId == nyOppgave.journalpostId }
            .forEach { repository[it.id] = it.copy(status = Oppgave.Status.FERDIGSTILT) }


        return nyOppgave
    }

    @PatchMapping("/api/v1/oppgaver/{id}")
    fun patchOppgave(@PathVariable id: Long, @RequestBody request: PatchOppgaveRequest): ResponseEntity<Oppgave> {
        logger.info("Patch oppgave {} {}", id, request)
        val oppgave = repository.get(id)
        if (oppgave == null) return ResponseEntity.notFound().build()
        val patchOppgaveGjelder= !request.behandlingstema.isNullOrBlank()  || !request.behandlingstype.isNullOrBlank()
        val patchedOppgave = oppgave.copy(
            versjon = request.versjon,
            status = request.status?.let { Oppgave.Status.valueOf(it.name) } ?: oppgave.status,
            tildeltEnhetsnr = request.tildeltEnhetsnr ?: oppgave.tildeltEnhetsnr,
            tilordnetRessurs = request.tilordnetRessurs ?: oppgave.tilordnetRessurs,
            fristFerdigstillelse = request.fristFerdigstillelse ?: oppgave.fristFerdigstillelse,
            journalpostId = request.journalpostId ?: oppgave.journalpostId,
            tema = request.tema ?: oppgave.tema,
            oppgavetype = request.oppgavetype ?: oppgave.oppgavetype,
            behandlingstema = if(patchOppgaveGjelder) request.behandlingstema else oppgave.behandlingstema,
            behandlingstype = if(patchOppgaveGjelder) request.behandlingstype else oppgave.behandlingstype,
        )
        repository.put(patchedOppgave.id, patchedOppgave)

        return ResponseEntity.ok(patchedOppgave)
    }
}