package no.nav.historisk.mock.tilgangsmaskin


import no.nav.tilgangsmaskin.Avvisningskode
import no.nav.tilgangsmaskin.ProblemDetaljResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("tilgangsmaskin-mock")
class TilgangsmaskinMockController {
    @PostMapping("/api/v1/komplett", "/api/v1/kjerne")
    fun komplett(@RequestBody personident: String): ResponseEntity<Any?> {
        // todo: vi behøver en personmodell som er konsistent med pdl-mocken.
        val avvisningskode: Avvisningskode? =
            if (personident.startsWith("6")) {
                Avvisningskode.AVVIST_STRENGT_FORTROLIG_ADRESSE
            } else if (personident.startsWith("7")) {
                Avvisningskode.AVVIST_SKJERMING
            } else {
                null
            }

        if (avvisningskode != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ProblemDetaljResponse(
                type = "mock",
                title = avvisningskode,
                status = HttpStatus.FORBIDDEN.value(),
                instance = "mock",
                brukerIdent = personident,
                navIdent = "MOCK-SERVER",
                begrunnelse = "Mock server begrunnelse",
                traceId = "mock",
                kanOverstyres = false
            ))
        }

        return ResponseEntity.ok().build<Any>()
    }
}