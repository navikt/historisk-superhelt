package no.nav.historisk.superhelt.sak.rest

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.endringslogg.EndringsloggService
import no.nav.historisk.superhelt.infrastruktur.authentication.getAuthenticatedUser
import no.nav.historisk.superhelt.person.MaskertPersonIdent
import no.nav.historisk.superhelt.sak.*
import no.nav.historisk.superhelt.sak.SakExtensions.auditLog
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/sak")
class SakController(
    private val sakService: SakService,
    private val sakRepository: SakRepository,
    private val endringsloggService: EndringsloggService,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Operation(operationId = "findSakerForPerson", summary = "Finn saker for en person")
    @GetMapping()
    fun findSaker(@RequestParam maskertPersonId: MaskertPersonIdent): ResponseEntity<List<Sak>> {
        val fnr = maskertPersonId.toFnr()
        val saker = sakRepository.findSaker(fnr)
        return ResponseEntity.ok(saker)
    }


    @Operation(operationId = "oppdaterSak")
    @PutMapping("{saksnummer}")
    fun oppdaterSak(
        @PathVariable saksnummer: Saksnummer,
        @RequestBody @Valid req: SakUpdateRequestDto,
    ): ResponseEntity<Sak> {
        val sak = sakRepository.getSak(saksnummer)

        val updateSakDto = UpdateSakDto(
            type = req.type,
            beskrivelse = req.beskrivelse,
            begrunnelse = req.begrunnelse,
            soknadsDato = req.soknadsDato,
            tildelingsAar = req.tildelingsAar,
            vedtaksResultat = req.vedtaksResultat,
            saksbehandler = getAuthenticatedUser().navUser
        )

        SakValidator(sak)
            .checkRettighet(SakRettighet.SAKSBEHANDLE)
            .checkUpdate(updateSakDto)
            .validate()

        val updated = sakRepository.updateSak(
            saksnummer, updateSakDto
        )
        return ResponseEntity.ok(updated)
    }

    @Operation(operationId = "oppdaterUtbetaling")
    @PutMapping("{saksnummer}/utbetaling")
    fun oppdaterUtbetaling(
        @PathVariable saksnummer: Saksnummer,
        @RequestBody @Valid req: UtbetalingRequestDto,
    ): ResponseEntity<Sak> {
        val sak = sakRepository.getSak(saksnummer)
        SakValidator(sak)
            .checkRettighet(SakRettighet.SAKSBEHANDLE)
            .validate()
        val updated = sakService.updateUtbetaling(saksnummer, req)
        return ResponseEntity.ok(updated)
    }

    @Operation(operationId = "getSakBySaksnummer", summary = "Hent opp en sak")
    @GetMapping("{saksnummer}")
    fun getSakBySaksnummer(@PathVariable saksnummer: Saksnummer): ResponseEntity<Sak> {
        val sak = sakRepository.getSak(saksnummer)
        SakValidator(sak)
            .checkRettighet(SakRettighet.LES)
            .validate()
        sak.auditLog("Hentet opp sak")
        return ResponseEntity.ok(sak)

    }


}
