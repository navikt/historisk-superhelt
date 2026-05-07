package no.nav.historisk.superhelt.klage

import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.klage.db.KabalEventEntity
import no.nav.historisk.superhelt.klage.db.KabalEventJpaRepository
import no.nav.kabal.model.BehandlingEvent
import no.nav.kabal.model.BehandlingEventType
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

/**
 * Lagrer og slår opp Kabal-events for idempotens og revisjon.
 */
@Repository
class KabalEventRepository(
    private val jpaRepository: KabalEventJpaRepository,
) {

    /**
     * Lagrer event atomisk med INSERT ... ON CONFLICT (event_id) DO NOTHING.
     * Returnerer true hvis eventet ble lagret (nytt), false hvis det var et duplikat.
     * Trådsikkert — race conditions ved parallell prosessering håndteres av databasen.
     */
    fun lagre(event: BehandlingEvent, saksnummer: String): Boolean =
        jpaRepository.insertOnConflictDoNothing(
            id = UUID.randomUUID(),
            eventId = event.eventId,
            saksnummer = saksnummer,
            eventType = event.type.name,
            utfall = event.utfall(),
            tidspunkt = event.tidspunkt(),
            aarsakFeilregistrert = event.detaljer.behandlingFeilregistrert?.reason,
            journalpostReferanser = event.journalpostReferanser().joinToString(","),
        ) > 0

    /** Returnerer alle Kabal-events for en sak, nyeste først. */
    fun hentHistorikkForSak(saksnummer: Saksnummer): List<KabalEventHistorikk> =
        jpaRepository.findBySaksnummerOrderByTidspunktDesc(saksnummer.value)
            .map { it.tilHistorikk() }
}

data class KabalEventHistorikk(
    val eventId: UUID,
    val saksnummer: String,
    val eventType: String,
    val utfall: String?,
    val tidspunkt: Instant,
    val aarsakFeilregistrert: String?,
    val journalpostReferanser: List<String>,
    val opprettetTid: Instant,
)

private fun KabalEventEntity.tilHistorikk() = KabalEventHistorikk(
    eventId = eventId,
    saksnummer = saksnummer,
    eventType = eventType,
    utfall = utfall,
    tidspunkt = tidspunkt,
    aarsakFeilregistrert = aarsakFeilregistrert,
    journalpostReferanser = journalpostReferanser
        ?.split(",")
        ?.filter { it.isNotBlank() }
        ?: emptyList(),
    opprettetTid = opprettetTid,
)

// ── Hjelpefunksjoner for å lese tvers av alle event-typer ────────────────────

internal fun BehandlingEvent.utfall(): String? =
    when (type) {
        BehandlingEventType.KLAGEBEHANDLING_AVSLUTTET ->
            detaljer.klagebehandlingAvsluttet?.utfall?.name

        BehandlingEventType.ANKEBEHANDLING_AVSLUTTET ->
            detaljer.ankebehandlingAvsluttet?.utfall?.name

        BehandlingEventType.BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET ->
            detaljer.behandlingEtterTrygderettenOpphevetAvsluttet?.utfall?.name

        BehandlingEventType.OMGJOERINGSKRAVBEHANDLING_AVSLUTTET ->
            detaljer.omgjoeringskravbehandlingAvsluttet?.utfall?.name

        BehandlingEventType.GJENOPPTAKSBEHANDLING_AVSLUTTET ->
            detaljer.gjenopptaksbehandlingAvsluttet?.utfall?.name

        BehandlingEventType.ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET ->
            detaljer.ankeITrygderettenbehandlingOpprettet?.utfall?.name

        else -> null
    }

internal fun BehandlingEvent.tidspunkt(): Instant =
    when (type) {
        BehandlingEventType.KLAGEBEHANDLING_AVSLUTTET ->
            detaljer.klagebehandlingAvsluttet?.avsluttet?.toInstant(java.time.ZoneOffset.UTC)

        BehandlingEventType.ANKEBEHANDLING_OPPRETTET ->
            detaljer.ankebehandlingOpprettet?.mottattKlageinstans?.toInstant(java.time.ZoneOffset.UTC)

        BehandlingEventType.ANKEBEHANDLING_AVSLUTTET ->
            detaljer.ankebehandlingAvsluttet?.avsluttet?.toInstant(java.time.ZoneOffset.UTC)

        BehandlingEventType.ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET ->
            detaljer.ankeITrygderettenbehandlingOpprettet?.sendtTilTrygderetten?.toInstant(java.time.ZoneOffset.UTC)

        BehandlingEventType.BEHANDLING_FEILREGISTRERT ->
            detaljer.behandlingFeilregistrert?.feilregistrert?.toInstant(java.time.ZoneOffset.UTC)

        BehandlingEventType.BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET ->
            detaljer.behandlingEtterTrygderettenOpphevetAvsluttet?.avsluttet?.toInstant(java.time.ZoneOffset.UTC)

        BehandlingEventType.OMGJOERINGSKRAVBEHANDLING_AVSLUTTET ->
            detaljer.omgjoeringskravbehandlingAvsluttet?.avsluttet?.toInstant(java.time.ZoneOffset.UTC)

        BehandlingEventType.GJENOPPTAKSBEHANDLING_AVSLUTTET ->
            detaljer.gjenopptaksbehandlingAvsluttet?.avsluttet?.toInstant(java.time.ZoneOffset.UTC)
    } ?: Instant.now()

internal fun BehandlingEvent.journalpostReferanser(): List<String> =
    when (type) {
        BehandlingEventType.KLAGEBEHANDLING_AVSLUTTET ->
            detaljer.klagebehandlingAvsluttet?.journalpostReferanser

        BehandlingEventType.ANKEBEHANDLING_AVSLUTTET ->
            detaljer.ankebehandlingAvsluttet?.journalpostReferanser

        BehandlingEventType.BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET ->
            detaljer.behandlingEtterTrygderettenOpphevetAvsluttet?.journalpostReferanser

        BehandlingEventType.OMGJOERINGSKRAVBEHANDLING_AVSLUTTET ->
            detaljer.omgjoeringskravbehandlingAvsluttet?.journalpostReferanser

        BehandlingEventType.GJENOPPTAKSBEHANDLING_AVSLUTTET ->
            detaljer.gjenopptaksbehandlingAvsluttet?.journalpostReferanser

        else -> null
    } ?: emptyList()

