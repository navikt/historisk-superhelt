package no.nav.historisk.superhelt.oppgave.rest

import io.swagger.v3.oas.annotations.Operation
import no.nav.common.types.NavIdent
import no.nav.historisk.superhelt.oppgave.OppgaveService
import no.nav.oppgave.model.Oppgave
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/oppgave")
class OppgaveController(private val oppgaveService: OppgaveService) {

    @Operation(operationId = "hentOppgaverForSaksbehandler")
    @GetMapping("{navident}")
    fun hentOppgaverForSaksbehandler(@PathVariable navident: NavIdent): List<Oppgave> {
        return oppgaveService.hentOppgaverForSaksbehandler(navident)
    }


}