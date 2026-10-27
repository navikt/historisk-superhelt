package no.nav.historisk.superhelt.oppgave.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import no.nav.common.types.EksternOppgaveId
import no.nav.historisk.superhelt.infrastruktur.authentication.getAuthenticatedUser
import no.nav.historisk.superhelt.oppgave.OppgaveMedSak
import no.nav.historisk.superhelt.oppgave.OppgaveService
import no.nav.historisk.superhelt.person.MaskertPersonIdent
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/oppgave")
class OppgaveController(private val oppgaveService: OppgaveService) {

    @Operation(operationId = "hentOppgaverForSaksbehandler")
    @GetMapping("saksbehandler")
    fun hentOppgaverForSaksbehandler(): List<OppgaveMedSak> {
        return oppgaveService.hentOppgaverForSaksbehandler()
    }

    @Operation(operationId = "hentOppgaverForPerson")
    @GetMapping("person/{maskertPersonIdent}")
    fun hentOppgaverForPerson(@PathVariable maskertPersonIdent: MaskertPersonIdent): List<OppgaveMedSak> {
        return oppgaveService.hentOppgaverForPerson(maskertPersonIdent.toFnr())
    }

    @Operation(operationId = "finnAttesteringsOppgaverForSaksbehandler")
    @GetMapping("attestering")
    fun finnAttesteringsOppgaverForSaksbehandler(): List<OppgaveMedSak> {
        return oppgaveService.finnAttesteringsOppgaverForSaksbehandler()
    }

    @Operation(operationId = "getOppgave")
    @GetMapping("{oppgaveId}")
    fun getOppgave(@PathVariable oppgaveId: EksternOppgaveId): OppgaveMedSak {
        return oppgaveService.getOppgave(oppgaveId)
    }

    @Operation(
        operationId = "tilordneOppaveTilSaksbehandler", responses =
            [ApiResponse(responseCode = "200", description = "Oppgave tilordnet saksbehandler"),
                ApiResponse(
                    responseCode = "409",
                    description = "Oppgaven er allerede tilordnet en annen saksbehandler",
                    content = [Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = ProblemDetail::class)
                    )]
                )]
    )
    @PatchMapping("{oppgaveId}/tilordnet")
    fun tilordneOppaveTilSaksbehandler(@PathVariable oppgaveId: EksternOppgaveId) {
        val navident = getAuthenticatedUser().navIdent
        oppgaveService.tilordneOppgave(oppgaveId = oppgaveId, tilordneTil = navident)
    }


}
