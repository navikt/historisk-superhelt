package no.nav.historisk.superhelt.oppgave.rest

import io.swagger.v3.oas.annotations.Operation
import no.nav.common.types.EksternOppgaveId
import no.nav.historisk.superhelt.infrastruktur.getCurrentNavIdent
import no.nav.historisk.superhelt.oppgave.OppgaveMedSak
import no.nav.historisk.superhelt.oppgave.OppgaveService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/oppgave")
class OppgaveController(private val oppgaveService: OppgaveService) {

    @Operation(operationId = "hentOppgaverForSaksbehandler")
    @GetMapping("saksbehandler")
    fun hentOppgaverForSaksbehandler(): List<OppgaveMedSak> {
        val navident = getCurrentNavIdent()
        return oppgaveService.hentOppgaverForSaksbehandler(navident)
    }

    @Operation(operationId = "getOppgave")
    @GetMapping("{oppgaveId}")
    fun getOppgave(@PathVariable oppgaveId: EksternOppgaveId): OppgaveMedSak {
        return oppgaveService.getOppgave(oppgaveId)
    }


}