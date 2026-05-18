package no.nav.historisk.superhelt.klage.kafka

import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.endringslogg.EndringsloggService
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.klage.KabalEventRepository
import no.nav.historisk.superhelt.klage.tidspunkt
import no.nav.historisk.superhelt.klage.utfall
import no.nav.historisk.superhelt.oppgave.OppgaveService
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.kabal.model.KabalBehandlingEvent
import no.nav.kabal.model.KabalBehandlingEventType
import no.nav.oppgave.OppgaveType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate


@Service
class KlageEventService(
    private val sakRepository: SakRepository,
    private val oppgaveService: OppgaveService,
    private val kabalEventRepository: KabalEventRepository,
    private val endringsloggService: EndringsloggService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun behandleEvent(event: KabalBehandlingEvent) {
        val saksnummer = Saksnummer(event.kildeReferanse)
        val sak=sakRepository.getSak(saksnummer) ?: run {
            logger.warn("Sak med saksnummer {} ikke funnet for Kabal-event {} (type={}) – ignorerer event.",
                saksnummer, event.eventId, event.type)
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
            event.eventId, event.type, event.utfall, saksnummer
        )
        val utfall = event.utfall
        if (utfall?.lagOppgave == true){
            opprettOppgaveMedDetaljer(event, sak)
        }

        loggTilEndringslogg(endringsloggService, saksnummer, event.type.tilEndringsloggType(), event)
    }


    /**
     * Bygger en dynamisk oppgave-beskrivelse basert på event-type og oppretter oppgaven.
     * Forutsetter at event-typen har utfall og avsluttet-tidspunkt i detaljer.
     */
    private fun opprettOppgaveMedDetaljer(event: KabalBehandlingEvent, sak: Sak) {
        val (utfall, avsluttet) = event.utfallOgAvsluttet()
            ?: error("${event.type} mangler detaljer for event ${event.eventId}")

        val behandlingsNavn = event.type.toNorsk()
        val beskrivelse = "$behandlingsNavn avsluttet i Kabal. Utfall: $utfall. Avsluttet: $avsluttet."

        logger.info("Oppretter oppgave for sak {} etter Kabal-event {} ({})", sak.saksnummer, event.eventId, event.type)
        oppgaveService.opprettOppgave(
            type = OppgaveType.VUR_KONS_YTE,
            sak = sak,
            beskrivelse = beskrivelse,
        )
    }
}

// ── Hjelpefunksjoner ──────────────────────────────────────────────────────────

/** Logg Kabal-event til endringslogg, slik at det vises i sak-historikken. */
private fun loggTilEndringslogg(
    endringsloggService: EndringsloggService,
    saksnummer: Saksnummer,
    endringstype: EndringsloggType,
    event: KabalBehandlingEvent,
    beskrivelse: String? = null,
) = endringsloggService.logChange(
    saksnummer = saksnummer,
    endringsType = endringstype,
    endring = event.type.toNorsk(),
    beskrivelse = beskrivelse ?: event.utfall?.let { "Utfall: $it" },
    tidspunkt = event.tidspunkt(),
)

/** Returnerer (utfall, avsluttet-dato) for event-typer som har dette i detaljer. */
private fun KabalBehandlingEvent.utfallOgAvsluttet(): Pair<String, LocalDate>? =
    when (type) {
        KabalBehandlingEventType.KLAGEBEHANDLING_AVSLUTTET ->
            detaljer.klagebehandlingAvsluttet?.let { it.utfall.name to it.avsluttet.toLocalDate() }

        KabalBehandlingEventType.ANKEBEHANDLING_AVSLUTTET ->
            detaljer.ankebehandlingAvsluttet?.let { it.utfall.name to it.avsluttet.toLocalDate() }

        KabalBehandlingEventType.BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET ->
            detaljer.behandlingEtterTrygderettenOpphevetAvsluttet?.let { it.utfall.name to it.avsluttet.toLocalDate() }

        KabalBehandlingEventType.OMGJOERINGSKRAVBEHANDLING_AVSLUTTET ->
            detaljer.omgjoeringskravbehandlingAvsluttet?.let { it.utfall.name to it.avsluttet.toLocalDate() }

        KabalBehandlingEventType.GJENOPPTAKSBEHANDLING_AVSLUTTET ->
            detaljer.gjenopptaksbehandlingAvsluttet?.let { it.utfall.name to it.avsluttet.toLocalDate() }

        else -> null
    }

/** Norsk visningsnavn for event-typen, brukt i oppgave-beskrivelsen. */
private fun KabalBehandlingEventType.toNorsk(): String =
    when (this) {
        KabalBehandlingEventType.KLAGEBEHANDLING_AVSLUTTET -> "Klagebehandling"
        KabalBehandlingEventType.ANKEBEHANDLING_AVSLUTTET -> "Ankebehandling"
        KabalBehandlingEventType.BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET -> "Behandling etter Trygderetten opphevet"
        KabalBehandlingEventType.OMGJOERINGSKRAVBEHANDLING_AVSLUTTET -> "Omgjøringskravbehandling"
        KabalBehandlingEventType.GJENOPPTAKSBEHANDLING_AVSLUTTET -> "Gjenopptaksbehandling"
        else -> name
    }

/** Mapper event-type til riktig EndringsloggType for logging. */
private fun KabalBehandlingEventType.tilEndringsloggType(): EndringsloggType =
    when (this) {
        KabalBehandlingEventType.KLAGEBEHANDLING_AVSLUTTET                          -> EndringsloggType.KABAL_BEHANDLING_AVSLUTTET
        KabalBehandlingEventType.ANKEBEHANDLING_OPPRETTET                           -> EndringsloggType.KABAL_BEHANDLING_OPPRETTET
        KabalBehandlingEventType.ANKEBEHANDLING_AVSLUTTET                           -> EndringsloggType.KABAL_BEHANDLING_AVSLUTTET
        KabalBehandlingEventType.ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET            -> EndringsloggType.KABAL_BEHANDLING_OPPRETTET
        KabalBehandlingEventType.BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET   -> EndringsloggType.KABAL_BEHANDLING_AVSLUTTET
        KabalBehandlingEventType.OMGJOERINGSKRAVBEHANDLING_AVSLUTTET                -> EndringsloggType.KABAL_BEHANDLING_AVSLUTTET
        KabalBehandlingEventType.GJENOPPTAKSBEHANDLING_AVSLUTTET                    -> EndringsloggType.KABAL_BEHANDLING_AVSLUTTET
        KabalBehandlingEventType.BEHANDLING_FEILREGISTRERT                          -> EndringsloggType.KABAL_BEHANDLING_FEILREGISTRERT
    }
