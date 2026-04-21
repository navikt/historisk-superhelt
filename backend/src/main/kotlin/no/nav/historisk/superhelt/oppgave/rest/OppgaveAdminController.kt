package no.nav.historisk.superhelt.oppgave.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.oppgave.OppgaveGjenopprettingService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Admin Oppgave", description = "Admin API for oppgaver")
@RequestMapping("/admin/oppgave")
class OppgaveAdminController(private val oppgaveGjenopprettingService: OppgaveGjenopprettingService) {


    @Operation(operationId = "gjenopprettOppgaver", summary = "Gjenopprett manglende oppgaver for saker")
    @PostMapping("gjenopprettOppgaver")
    fun gjenopprettOppgaver(): List<Saksnummer> {
        return oppgaveGjenopprettingService.gjenopprettManglendeOppgaver()
    }
}
