package no.nav.historisk.mock.kabal

import no.nav.kabal.model.AnkeUtfall
import no.nav.kabal.model.FeilregistrertBehandlingType
import no.nav.kabal.model.GjenopptaksUtfall
import no.nav.kabal.model.KlageUtfall
import no.nav.kabal.model.OmgjoeringskravUtfall
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("kabal-mock")
class KabalMockController(
    private val producer: MockKabalBehandlingEventProducer,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /** Mottaker for klagesending fra backend (simulerer Kabal REST-API) */
    @PostMapping("/api/oversendelse/v4/sak")
    fun sendSak(@RequestBody body: Map<String, Any?>): ResponseEntity<Void> {
        logger.info("Kabal mock: mottatt klagesending: $body")
        return ResponseEntity.ok().build()
    }

    // ── 1. KLAGEBEHANDLING_AVSLUTTET ────────────────────────────────────────

    @PostMapping("/kafka/klage/{kildeReferanse}")
    fun simulerKlageAvsluttet(
        @PathVariable kildeReferanse: String,
        @RequestParam(defaultValue = "MEDHOLD") utfall: String,
    ): ResponseEntity<String> {
        val klageUtfall = runCatching { KlageUtfall.valueOf(utfall) }.getOrElse {
            return ResponseEntity.badRequest()
                .body("Ugyldig utfall '$utfall'. Gyldige verdier: ${KlageUtfall.entries.joinToString()}")
        }
        val event = KabalTestdata.lagKlagebehandlingAvsluttetEvent(kildeReferanse, klageUtfall)
        producer.sendEvent(event)
        logger.info("Sendte KLAGEBEHANDLING_AVSLUTTET kildeReferanse={} utfall={}", kildeReferanse, klageUtfall)
        return ResponseEntity.ok("Sendte KLAGEBEHANDLING_AVSLUTTET for $kildeReferanse med utfall=$klageUtfall")
    }

    // ── 2. ANKEBEHANDLING_OPPRETTET ─────────────────────────────────────────

    @PostMapping("/kafka/anke-opprettet/{kildeReferanse}")
    fun simulerAnkeOpprettet(
        @PathVariable kildeReferanse: String,
    ): ResponseEntity<String> {
        val event = KabalTestdata.lagAnkebehandlingOpprettetEvent(kildeReferanse)
        producer.sendEvent(event)
        logger.info("Sendte ANKEBEHANDLING_OPPRETTET kildeReferanse={}", kildeReferanse)
        return ResponseEntity.ok("Sendte ANKEBEHANDLING_OPPRETTET for $kildeReferanse")
    }

    // ── 3. ANKEBEHANDLING_AVSLUTTET ─────────────────────────────────────────

    @PostMapping("/kafka/anke/{kildeReferanse}")
    fun simulerAnkeAvsluttet(
        @PathVariable kildeReferanse: String,
        @RequestParam(defaultValue = "STADFESTELSE") utfall: String,
    ): ResponseEntity<String> {
        val ankeUtfall = runCatching { AnkeUtfall.valueOf(utfall) }.getOrElse {
            return ResponseEntity.badRequest()
                .body("Ugyldig utfall '$utfall'. Gyldige verdier: ${AnkeUtfall.entries.joinToString()}")
        }
        val event = KabalTestdata.lagAnkebehandlingAvsluttetEvent(kildeReferanse, ankeUtfall)
        producer.sendEvent(event)
        logger.info("Sendte ANKEBEHANDLING_AVSLUTTET kildeReferanse={} utfall={}", kildeReferanse, ankeUtfall)
        return ResponseEntity.ok("Sendte ANKEBEHANDLING_AVSLUTTET for $kildeReferanse med utfall=$ankeUtfall")
    }

    // ── 4. ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET ──────────────────────────

    @PostMapping("/kafka/anke-trygderetten/{kildeReferanse}")
    fun simulerAnkeITrygderettenOpprettet(
        @PathVariable kildeReferanse: String,
    ): ResponseEntity<String> {
        val event = KabalTestdata.lagAnkeITrygderettenOpprettetEvent(kildeReferanse)
        producer.sendEvent(event)
        logger.info("Sendte ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET kildeReferanse={}", kildeReferanse)
        return ResponseEntity.ok("Sendte ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET for $kildeReferanse")
    }

    // ── 5. BEHANDLING_FEILREGISTRERT ────────────────────────────────────────

    @PostMapping("/kafka/feilregistrert/{kildeReferanse}")
    fun simulerBehandlingFeilregistrert(
        @PathVariable kildeReferanse: String,
        @RequestParam(defaultValue = "KLAGE") type: String,
        @RequestParam(defaultValue = "Z999999") navIdent: String,
        @RequestParam(defaultValue = "Feilregistrert under testing") reason: String,
    ): ResponseEntity<String> {
        val feilType = runCatching { FeilregistrertBehandlingType.valueOf(type) }.getOrElse {
            return ResponseEntity.badRequest()
                .body("Ugyldig type '$type'. Gyldige verdier: ${FeilregistrertBehandlingType.entries.joinToString()}")
        }
        val event = KabalTestdata.lagBehandlingFeilregistrertEvent(kildeReferanse, feilType, navIdent, reason)
        producer.sendEvent(event)
        logger.info("Sendte BEHANDLING_FEILREGISTRERT kildeReferanse={} type={}", kildeReferanse, feilType)
        return ResponseEntity.ok("Sendte BEHANDLING_FEILREGISTRERT for $kildeReferanse type=$feilType")
    }

    // ── 6. BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET ─────────────────

    @PostMapping("/kafka/etter-trygderetten/{kildeReferanse}")
    fun simulerBehandlingEtterTrygderettenOpphevet(
        @PathVariable kildeReferanse: String,
        @RequestParam(defaultValue = "MEDHOLD") utfall: String,
    ): ResponseEntity<String> {
        val klageUtfall = runCatching { KlageUtfall.valueOf(utfall) }.getOrElse {
            return ResponseEntity.badRequest()
                .body("Ugyldig utfall '$utfall'. Gyldige verdier: ${KlageUtfall.entries.joinToString()}")
        }
        val event = KabalTestdata.lagBehandlingEtterTrygderettenOpphevetAvsluttetEvent(kildeReferanse, klageUtfall)
        producer.sendEvent(event)
        logger.info("Sendte BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET kildeReferanse={} utfall={}", kildeReferanse, klageUtfall)
        return ResponseEntity.ok("Sendte BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET for $kildeReferanse med utfall=$klageUtfall")
    }

    // ── 7. OMGJOERINGSKRAVBEHANDLING_AVSLUTTET ──────────────────────────────

    @PostMapping("/kafka/omgjoeringskrav/{kildeReferanse}")
    fun simulerOmgjoeringskravbehandlingAvsluttet(
        @PathVariable kildeReferanse: String,
        @RequestParam(defaultValue = "MEDHOLD_ETTER_FVL_35") utfall: String,
    ): ResponseEntity<String> {
        val omgjUtfall = runCatching { OmgjoeringskravUtfall.valueOf(utfall) }.getOrElse {
            return ResponseEntity.badRequest()
                .body("Ugyldig utfall '$utfall'. Gyldige verdier: ${OmgjoeringskravUtfall.entries.joinToString()}")
        }
        val event = KabalTestdata.lagOmgjoeringskravbehandlingAvsluttetEvent(kildeReferanse, omgjUtfall)
        producer.sendEvent(event)
        logger.info("Sendte OMGJOERINGSKRAVBEHANDLING_AVSLUTTET kildeReferanse={} utfall={}", kildeReferanse, omgjUtfall)
        return ResponseEntity.ok("Sendte OMGJOERINGSKRAVBEHANDLING_AVSLUTTET for $kildeReferanse med utfall=$omgjUtfall")
    }

    // ── 8. GJENOPPTAKSBEHANDLING_AVSLUTTET ──────────────────────────────────

    @PostMapping("/kafka/gjenopptak/{kildeReferanse}")
    fun simulerGjenopptaksbehandlingAvsluttet(
        @PathVariable kildeReferanse: String,
        @RequestParam(defaultValue = "GJENOPPTATT_DELVIS_ELLER_FULLT_MEDHOLD") utfall: String,
    ): ResponseEntity<String> {
        val gjenUtfall = runCatching { GjenopptaksUtfall.valueOf(utfall) }.getOrElse {
            return ResponseEntity.badRequest()
                .body("Ugyldig utfall '$utfall'. Gyldige verdier: ${GjenopptaksUtfall.entries.joinToString()}")
        }
        val event = KabalTestdata.lagGjenopptaksbehandlingAvsluttetEvent(kildeReferanse, gjenUtfall)
        producer.sendEvent(event)
        logger.info("Sendte GJENOPPTAKSBEHANDLING_AVSLUTTET kildeReferanse={} utfall={}", kildeReferanse, gjenUtfall)
        return ResponseEntity.ok("Sendte GJENOPPTAKSBEHANDLING_AVSLUTTET for $kildeReferanse med utfall=$gjenUtfall")
    }
}
