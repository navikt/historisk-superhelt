package no.nav.historisk.superhelt.klage.kafka

import no.nav.common.types.NavIdent
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.endringslogg.EndringsloggService
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.klage.KabalEventRepository
import no.nav.historisk.superhelt.klage.tidspunkt
import no.nav.historisk.superhelt.klage.utfall
import no.nav.historisk.superhelt.oppgave.OppgaveService
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.kabal.model.BehandlingEvent
import no.nav.kabal.model.BehandlingEventType
import no.nav.oppgave.OppgaveType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

private val KABAL_SYSTEM_IDENT = NavIdent("kabal-event-system")

@Service
class KlageEventService(
    private val sakRepository: SakRepository,
    private val oppgaveService: OppgaveService,
    private val kabalEventRepository: KabalEventRepository,
    private val endringsloggService: EndringsloggService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun behandleEvent(event: BehandlingEvent) {
        val saksnummer = runCatching { Saksnummer(event.kildeReferanse) }.getOrElse {
            logger.error("Ugyldig kildeReferanse '{}' i BehandlingEvent {}", event.kildeReferanse, event.eventId)
            return
        }

        val sak = runCatching { sakRepository.getSak(saksnummer) }.getOrElse {
            logger.error(
                "Fant ikke sak {} fra Kabal-event {} (type={}). Ignorerer.",
                saksnummer, event.eventId, event.type
            )
            return
        }

        val erNytt = kabalEventRepository.lagre(event, saksnummer.value)
        if (!erNytt) {
            logger.warn(
                "Kabal-event {} (type={}) er allerede prosessert – ignorerer duplikat.",
                event.eventId, event.type
            )
            return
        }

        logger.info(
            "Prosesserer Kabal-event {} (type={}, utfall={}) for sak {}",
            event.eventId, event.type, event.utfall(), saksnummer
        )

        when (event.type) {
            // ── Klagebehandling: sjekk saksstatus før oppgave opprettes ──────────────
            BehandlingEventType.KLAGEBEHANDLING_AVSLUTTET ->
                if (sak.status == SakStatus.FEILREGISTRERT) {
                    logger.error(
                        "Mottatt KLAGEBEHANDLING_AVSLUTTET-event på sak {} med status {} – " +
                            "saken er feilregistrert. Oppretter ikke oppgave.",
                        saksnummer, sak.status
                    )
                } else {
                    opprettOppgaveMedDetaljer(event, saksnummer)
                }

            // ── Avsluttede behandlinger som alltid medfører oppgave ──────────────────
            BehandlingEventType.ANKEBEHANDLING_AVSLUTTET,
            BehandlingEventType.BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET,
            BehandlingEventType.OMGJOERINGSKRAVBEHANDLING_AVSLUTTET,
            BehandlingEventType.GJENOPPTAKSBEHANDLING_AVSLUTTET,
            -> opprettOppgaveMedDetaljer(event, saksnummer)

            // ── Feilregistrert: marker saken i databasen ────────────────────────────
            BehandlingEventType.BEHANDLING_FEILREGISTRERT -> {
                val detaljer = event.detaljer.behandlingFeilregistrert
                    ?: error("BEHANDLING_FEILREGISTRERT mangler detaljer for event ${event.eventId}")
                logger.info(
                    "Markerer sak {} som feilregistrert etter Kabal-event {}. Registrert av: {}, årsak: {}",
                    saksnummer, event.eventId, detaljer.navIdent, detaljer.reason
                )
                sakRepository.feilregistrerSak(saksnummer)
                loggTilEndringslogg(
                    endringsloggService, saksnummer, event.type.tilEndringsloggType(), event,
                    beskrivelse = "Registrert av: ${detaljer.navIdent}. Årsak: ${detaljer.reason}",
                )
                return
            }

            // ── Ingen aksjon kreves for disse event-typene ───────────────────────────
            BehandlingEventType.ANKEBEHANDLING_OPPRETTET,
            BehandlingEventType.ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET,
            -> logger.info(
                "Mottatt Kabal-event {} av type {} for sak {} – ingen aksjon kreves.",
                event.eventId, event.type, saksnummer,
            )
        }

        // Alltid logg til sak-historikk (unntatt FEILREGISTRERT som returnerer tidlig med egen beskrivelse)
        loggTilEndringslogg(endringsloggService, saksnummer, event.type.tilEndringsloggType(), event)
    }


    /**
     * Bygger en dynamisk oppgave-beskrivelse basert på event-type og oppretter oppgaven.
     * Forutsetter at event-typen har utfall og avsluttet-tidspunkt i detaljer.
     */
    private fun opprettOppgaveMedDetaljer(event: BehandlingEvent, saksnummer: Saksnummer) {
        val (utfall, avsluttet) = event.utfallOgAvsluttet()
            ?: error("${event.type} mangler detaljer for event ${event.eventId}")

        val behandlingsNavn = event.type.toNorsk()
        val beskrivelse = "$behandlingsNavn avsluttet i Kabal. Utfall: $utfall. Avsluttet: $avsluttet."

        val sak = sakRepository.getSak(saksnummer)
        logger.info("Oppretter oppgave for sak {} etter Kabal-event {} ({})", saksnummer, event.eventId, event.type)
        oppgaveService.opprettOppgave(
            type = OppgaveType.VUR_KONS_YTE,
            sak = sak,
            beskrivelse = beskrivelse,
            behandlesAvApplikasjon = "SUPERHELT",
        )
    }
}

// ── Hjelpefunksjoner ──────────────────────────────────────────────────────────

/** Logg Kabal-event til endringslogg, slik at det vises i sak-historikken. */
private fun loggTilEndringslogg(
    endringsloggService: EndringsloggService,
    saksnummer: Saksnummer,
    type: EndringsloggType,
    event: BehandlingEvent,
    beskrivelse: String? = null,
) = endringsloggService.logChange(
    saksnummer = saksnummer,
    endringsType = type,
    navBruker = KABAL_SYSTEM_IDENT,
    endring = event.type.toNorsk(),
    beskrivelse = beskrivelse ?: event.utfall()?.let { "Utfall: $it" },
    tidspunkt = event.tidspunkt(),
)

/** Returnerer (utfall, avsluttet-dato) for event-typer som har dette i detaljer. */
private fun BehandlingEvent.utfallOgAvsluttet(): Pair<String, LocalDate>? =
    when (type) {
        BehandlingEventType.KLAGEBEHANDLING_AVSLUTTET ->
            detaljer.klagebehandlingAvsluttet?.let { it.utfall.name to it.avsluttet.toLocalDate() }

        BehandlingEventType.ANKEBEHANDLING_AVSLUTTET ->
            detaljer.ankebehandlingAvsluttet?.let { it.utfall.name to it.avsluttet.toLocalDate() }

        BehandlingEventType.BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET ->
            detaljer.behandlingEtterTrygderettenOpphevetAvsluttet?.let { it.utfall.name to it.avsluttet.toLocalDate() }

        BehandlingEventType.OMGJOERINGSKRAVBEHANDLING_AVSLUTTET ->
            detaljer.omgjoeringskravbehandlingAvsluttet?.let { it.utfall.name to it.avsluttet.toLocalDate() }

        BehandlingEventType.GJENOPPTAKSBEHANDLING_AVSLUTTET ->
            detaljer.gjenopptaksbehandlingAvsluttet?.let { it.utfall.name to it.avsluttet.toLocalDate() }

        else -> null
    }

/** Norsk visningsnavn for event-typen, brukt i oppgave-beskrivelsen. */
private fun BehandlingEventType.toNorsk(): String =
    when (this) {
        BehandlingEventType.KLAGEBEHANDLING_AVSLUTTET -> "Klagebehandling"
        BehandlingEventType.ANKEBEHANDLING_AVSLUTTET -> "Ankebehandling"
        BehandlingEventType.BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET -> "Behandling etter Trygderetten opphevet"
        BehandlingEventType.OMGJOERINGSKRAVBEHANDLING_AVSLUTTET -> "Omgjøringskravbehandling"
        BehandlingEventType.GJENOPPTAKSBEHANDLING_AVSLUTTET -> "Gjenopptaksbehandling"
        else -> name
    }

/** Mapper event-type til riktig EndringsloggType for logging. */
private fun BehandlingEventType.tilEndringsloggType(): EndringsloggType =
    when (this) {
        BehandlingEventType.KLAGEBEHANDLING_AVSLUTTET                          -> EndringsloggType.KABAL_BEHANDLING_AVSLUTTET
        BehandlingEventType.ANKEBEHANDLING_OPPRETTET                           -> EndringsloggType.KABAL_BEHANDLING_OPPRETTET
        BehandlingEventType.ANKEBEHANDLING_AVSLUTTET                           -> EndringsloggType.KABAL_BEHANDLING_AVSLUTTET
        BehandlingEventType.ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET            -> EndringsloggType.KABAL_BEHANDLING_OPPRETTET
        BehandlingEventType.BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET   -> EndringsloggType.KABAL_BEHANDLING_AVSLUTTET
        BehandlingEventType.OMGJOERINGSKRAVBEHANDLING_AVSLUTTET                -> EndringsloggType.KABAL_BEHANDLING_AVSLUTTET
        BehandlingEventType.GJENOPPTAKSBEHANDLING_AVSLUTTET                    -> EndringsloggType.KABAL_BEHANDLING_AVSLUTTET
        BehandlingEventType.BEHANDLING_FEILREGISTRERT                          -> EndringsloggType.KABAL_BEHANDLING_FEILREGISTRERT
    }
