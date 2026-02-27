package no.nav.historisk.superhelt.sak.rest

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.infrastruktur.authentication.getAuthenticatedUser
import no.nav.historisk.superhelt.person.MaskertPersonIdent
import no.nav.historisk.superhelt.sak.*
import no.nav.historisk.superhelt.sak.SakExtensions.auditLog
import no.nav.historisk.superhelt.utbetaling.UtbetalingRepository
import no.nav.historisk.superhelt.utbetaling.UtbetalingStatus
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/sak")
class SakController(
    private val sakRepository: SakRepository,
    private val utbetalingRepository: UtbetalingRepository,
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
            saksbehandler = getAuthenticatedUser().navUser,
            utbetalingsType = req.utbetalingsType,
            belop = req.belop,
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

    @Operation(operationId = "getSakStatus", summary = "Hent aggregert status for sak, utbetaling og brev")
    @GetMapping("{saksnummer}/status")
    fun getSakStatus(@PathVariable saksnummer: Saksnummer): ResponseEntity<SakStatusDto> {
        val sak = sakRepository.getSak(saksnummer)
        SakValidator(sak).checkRettighet(SakRettighet.LES).validate()

        val utbetalingStatus = utbetalingRepository.findActiveBySaksnummer(saksnummer)?.utbetalingStatus
        val brevStatus = sak.vedtaksbrevBruker?.status

        val aggregertStatus = if (utbetalingStatus == UtbetalingStatus.FEILET) AggregertSakStatus.FEILET
                              else AggregertSakStatus.OK

        return ResponseEntity.ok(
            SakStatusDto(
                sakStatus = sak.status,
                utbetalingStatus = utbetalingStatus,
                brevStatus = brevStatus,
                aggregertStatus = aggregertStatus,
            )
        )
    }
}
