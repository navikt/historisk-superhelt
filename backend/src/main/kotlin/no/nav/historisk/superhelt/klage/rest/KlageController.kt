package no.nav.historisk.superhelt.klage.rest

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.endringslogg.EndringsloggService
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.klage.KlageService
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakRettighet
import no.nav.historisk.superhelt.sak.SakValidator
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
class KlageController(
    private val sakRepository: SakRepository,
    private val klageService: KlageService,
    private val endringsloggService: EndringsloggService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Operation(operationId = "sendKlageTilKabal")
    @PostMapping("/api/sak/{saksnummer}/klage")
    fun sendKlage(
        @PathVariable saksnummer: Saksnummer,
        @Valid @RequestBody request: SendKlageRequestDto,
    ): ResponseEntity<Void> {
        val sak = sakRepository.getSak(saksnummer)
        SakValidator(sak)
            .checkRettighet(SakRettighet.SEND_KLAGE)
            .validate()

        logger.info("Sender klage til Kabal for sak $saksnummer")
        klageService.sendKlage(sak, request)

        endringsloggService.logChange(
            saksnummer = saksnummer,
            endringsType = EndringsloggType.KLAGE_SENDT_KABAL,
            endring = "Klage sendt til Kabal",
            beskrivelse = buildString {
                append("Hjemmel: ${request.hjemmelId}")
                append(", Dato klage mottatt: ${request.datoKlageMottatt}")
                request.kommentar?.let { append(", Kommentar: $it") }
            },
        )

        return ResponseEntity.noContent().build()
    }
}
