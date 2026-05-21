package no.nav.historisk.superhelt.klage

import no.nav.common.consts.FellesKodeverkTema
import no.nav.historisk.superhelt.StonadsType
import no.nav.kabal.model.KabalBehandlingEvent
import no.nav.kabal.model.KabalBehandlingEventType
import no.nav.kabal.model.KabalUtfall
import no.nav.kabal.model.KabalYtelse
import java.time.Instant

val StonadsType.kabalYtelse: KabalYtelse
    get() =
        when {
            this == StonadsType.ARBEID_UTDANNING -> KabalYtelse.HJE_AUR
            this.tema == FellesKodeverkTema.HEL -> KabalYtelse.HEL_HEL
            this.tema == FellesKodeverkTema.HJE -> KabalYtelse.HJE_HJE
            else -> {
                throw IllegalArgumentException("Ukjent Kabal-ytelse for stonadstype ${this.navn}")
            }
        }


// ── Hjelpefunksjoner for å lese tvers av alle event-typer ────────────────────


internal val KabalBehandlingEvent.utfall: KabalUtfall?
    get() = when (type) {
        KabalBehandlingEventType.KLAGEBEHANDLING_AVSLUTTET ->
            detaljer.klagebehandlingAvsluttet?.utfall?.name?.let { KabalUtfall.valueOf(it) }
        KabalBehandlingEventType.ANKEBEHANDLING_AVSLUTTET ->
            detaljer.ankebehandlingAvsluttet?.utfall?.name?.let { KabalUtfall.valueOf(it) }

        KabalBehandlingEventType.BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET ->
            detaljer.behandlingEtterTrygderettenOpphevetAvsluttet?.utfall?.name?.let { KabalUtfall.valueOf(it) }

        KabalBehandlingEventType.OMGJOERINGSKRAVBEHANDLING_AVSLUTTET ->
            detaljer.omgjoeringskravbehandlingAvsluttet?.utfall?.name?.let { KabalUtfall.valueOf(it) }

        KabalBehandlingEventType.GJENOPPTAKSBEHANDLING_AVSLUTTET ->
            detaljer.gjenopptaksbehandlingAvsluttet?.utfall?.name?.let { KabalUtfall.valueOf(it) }

        KabalBehandlingEventType.ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET ->
            detaljer.ankeITrygderettenbehandlingOpprettet?.utfall?.name?.let { KabalUtfall.valueOf(it) }

        else -> null
    }


internal fun KabalBehandlingEvent.tidspunkt(): Instant =
    when (type) {
        KabalBehandlingEventType.KLAGEBEHANDLING_AVSLUTTET ->
            detaljer.klagebehandlingAvsluttet?.avsluttet?.toInstant(java.time.ZoneOffset.UTC)

        KabalBehandlingEventType.ANKEBEHANDLING_OPPRETTET ->
            detaljer.ankebehandlingOpprettet?.mottattKlageinstans?.toInstant(java.time.ZoneOffset.UTC)

        KabalBehandlingEventType.ANKEBEHANDLING_AVSLUTTET ->
            detaljer.ankebehandlingAvsluttet?.avsluttet?.toInstant(java.time.ZoneOffset.UTC)

        KabalBehandlingEventType.ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET ->
            detaljer.ankeITrygderettenbehandlingOpprettet?.sendtTilTrygderetten?.toInstant(java.time.ZoneOffset.UTC)

        KabalBehandlingEventType.BEHANDLING_FEILREGISTRERT ->
            detaljer.behandlingFeilregistrert?.feilregistrert?.toInstant(java.time.ZoneOffset.UTC)

        KabalBehandlingEventType.BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET ->
            detaljer.behandlingEtterTrygderettenOpphevetAvsluttet?.avsluttet?.toInstant(java.time.ZoneOffset.UTC)

        KabalBehandlingEventType.OMGJOERINGSKRAVBEHANDLING_AVSLUTTET ->
            detaljer.omgjoeringskravbehandlingAvsluttet?.avsluttet?.toInstant(java.time.ZoneOffset.UTC)

        KabalBehandlingEventType.GJENOPPTAKSBEHANDLING_AVSLUTTET ->
            detaljer.gjenopptaksbehandlingAvsluttet?.avsluttet?.toInstant(java.time.ZoneOffset.UTC)
    } ?: Instant.now()

internal fun KabalBehandlingEvent.journalpostReferanser(): List<String> =
    when (type) {
        KabalBehandlingEventType.KLAGEBEHANDLING_AVSLUTTET ->
            detaljer.klagebehandlingAvsluttet?.journalpostReferanser

        KabalBehandlingEventType.ANKEBEHANDLING_AVSLUTTET ->
            detaljer.ankebehandlingAvsluttet?.journalpostReferanser

        KabalBehandlingEventType.BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET ->
            detaljer.behandlingEtterTrygderettenOpphevetAvsluttet?.journalpostReferanser

        KabalBehandlingEventType.OMGJOERINGSKRAVBEHANDLING_AVSLUTTET ->
            detaljer.omgjoeringskravbehandlingAvsluttet?.journalpostReferanser

        KabalBehandlingEventType.GJENOPPTAKSBEHANDLING_AVSLUTTET ->
            detaljer.gjenopptaksbehandlingAvsluttet?.journalpostReferanser

        else -> null
    } ?: emptyList()
