package no.nav.historisk.superhelt.oppgave.rest

import io.swagger.v3.oas.annotations.Operation
import no.nav.oppgave.OppgaveGjelder
import no.nav.oppgave.OppgaveType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/oppgave/kodeverk")
class OppgaveKodeverkController {

    @Operation(operationId = "getKodeverkOppgaveGjelder")
    @GetMapping("oppgavegjelder")
    fun gjelderKodeverk(): List<OppgaveGjelderKodeDto> {
        return OppgaveGjelder.entries.map { OppgaveGjelderKodeDto(it) }
    }

    @Operation(operationId = "getKodeverkOppgaveType")
    @GetMapping("oppgavetype")
    fun oppgaveTypeKodeverk(): List<OppgaveTypeKodeDto> {
        return OppgaveType.entries.map { OppgaveTypeKodeDto(it) }
    }

    data class OppgaveGjelderKodeDto(val type: OppgaveGjelder) {
        val navn: String = type.stringValue
    }

    data class OppgaveTypeKodeDto(val type: OppgaveType) {
        val navn: String = type.beskrivelse
    }
}
