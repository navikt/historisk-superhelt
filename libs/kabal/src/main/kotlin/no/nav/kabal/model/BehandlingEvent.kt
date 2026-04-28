package no.nav.kabal.model

import java.time.LocalDateTime
import java.util.UUID

/**
 * Kafka-melding fra Kabal på topic behandling-events.v1.
 * Schema: https://github.com/navikt/kabal-api/blob/main/docs/schema/behandling-events.json
 */
data class BehandlingEvent(
    val eventId: UUID,
    /** Ekstern id sendt inn – matcher kildeReferanse i SendSakV4Request */
    val kildeReferanse: String,
    /** Kildesystem – matcher fagsystem sendt inn, f.eks. "SUPERHELT" */
    val kilde: String,
    /** Intern referanse i Kabal */
    val kabalReferanse: String,
    val type: BehandlingEventType,
    val detaljer: BehandlingDetaljer,
)

enum class BehandlingEventType {
    KLAGEBEHANDLING_AVSLUTTET,
    ANKEBEHANDLING_OPPRETTET,
    ANKEBEHANDLING_AVSLUTTET,
    ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET,
    BEHANDLING_FEILREGISTRERT,
    BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET,
    OMGJOERINGSKRAVBEHANDLING_AVSLUTTET,
    GJENOPPTAKSBEHANDLING_AVSLUTTET,
}

data class BehandlingDetaljer(
    val klagebehandlingAvsluttet: KlagebehandlingAvsluttetDetaljer? = null,
    val ankebehandlingOpprettet: AnkebehandlingOpprettetDetaljer? = null,
    val ankebehandlingAvsluttet: AnkebehandlingAvsluttetDetaljer? = null,
    val ankeITrygderettenbehandlingOpprettet: AnkeITrygderettenbehandlingOpprettetDetaljer? = null,
    val behandlingFeilregistrert: BehandlingFeilregistrertDetaljer? = null,
    val behandlingEtterTrygderettenOpphevetAvsluttet: BehandlingEtterTrygderettenOpphevetAvsluttetDetaljer? = null,
    val omgjoeringskravbehandlingAvsluttet: OmgjoeringskravbehandlingAvsluttetDetaljer? = null,
    val gjenopptaksbehandlingAvsluttet: GjenopptaksbehandlingAvsluttetDetaljer? = null,
)

data class KlagebehandlingAvsluttetDetaljer(
    val avsluttet: LocalDateTime,
    val utfall: KlageUtfall,
    val journalpostReferanser: List<String>,
)

enum class KlageUtfall {
    TRUKKET, RETUR, OPPHEVET, MEDHOLD, DELVIS_MEDHOLD, STADFESTELSE, UGUNST, AVVIST, HENLAGT,
}

data class AnkebehandlingOpprettetDetaljer(
    val mottattKlageinstans: LocalDateTime,
)

data class AnkebehandlingAvsluttetDetaljer(
    val avsluttet: LocalDateTime,
    val utfall: AnkeUtfall,
    val journalpostReferanser: List<String>,
)

enum class AnkeUtfall {
    TRUKKET, OPPHEVET, MEDHOLD, DELVIS_MEDHOLD, STADFESTELSE, UGUNST, AVVIST, HEVET, HENVIST, HENLAGT,
}

data class AnkeITrygderettenbehandlingOpprettetDetaljer(
    val sendtTilTrygderetten: LocalDateTime,
    val utfall: AnkeITrygderettenUtfall? = null,
)

enum class AnkeITrygderettenUtfall {
    DELVIS_MEDHOLD, INNSTILLING_STADFESTELSE, INNSTILLING_AVVIST,
}

data class BehandlingFeilregistrertDetaljer(
    val feilregistrert: LocalDateTime,
    val navIdent: String,
    val reason: String,
    val type: FeilregistrertBehandlingType,
)

enum class FeilregistrertBehandlingType {
    KLAGE, ANKE, ANKE_I_TRYGDERETTEN, BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET,
    OMGJOERINGSKRAV, BEGJAERING_OM_GJENOPPTAK, BEGJAERING_OM_GJENOPPTAK_I_TRYGDERETTEN,
}

data class BehandlingEtterTrygderettenOpphevetAvsluttetDetaljer(
    val avsluttet: LocalDateTime,
    val utfall: KlageUtfall,
    val journalpostReferanser: List<String>,
)

data class OmgjoeringskravbehandlingAvsluttetDetaljer(
    val avsluttet: LocalDateTime,
    val utfall: OmgjoeringskravUtfall,
    val journalpostReferanser: List<String>,
)

enum class OmgjoeringskravUtfall {
    MEDHOLD_ETTER_FVL_35, UGUNST,
}

data class GjenopptaksbehandlingAvsluttetDetaljer(
    val avsluttet: LocalDateTime,
    val utfall: GjenopptaksUtfall,
    val journalpostReferanser: List<String>,
)

enum class GjenopptaksUtfall {
    MEDHOLD_ETTER_FVL_35, GJENOPPTATT_DELVIS_ELLER_FULLT_MEDHOLD, GJENOPPTATT_OPPHEVET, UGUNST,
}

