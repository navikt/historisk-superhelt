package no.nav.historisk.mock.kabal

import net.datafaker.Faker
import no.nav.kabal.model.AnkeITrygderettenbehandlingOpprettetDetaljer
import no.nav.kabal.model.AnkebehandlingAvsluttetDetaljer
import no.nav.kabal.model.AnkebehandlingOpprettetDetaljer
import no.nav.kabal.model.AnkeUtfall
import no.nav.kabal.model.BehandlingDetaljer
import no.nav.kabal.model.BehandlingEtterTrygderettenOpphevetAvsluttetDetaljer
import no.nav.kabal.model.BehandlingEvent
import no.nav.kabal.model.BehandlingEventType
import no.nav.kabal.model.BehandlingFeilregistrertDetaljer
import no.nav.kabal.model.FeilregistrertBehandlingType
import no.nav.kabal.model.GjenopptaksbehandlingAvsluttetDetaljer
import no.nav.kabal.model.GjenopptaksUtfall
import no.nav.kabal.model.KlageUtfall
import no.nav.kabal.model.KlagebehandlingAvsluttetDetaljer
import no.nav.kabal.model.OmgjoeringskravbehandlingAvsluttetDetaljer
import no.nav.kabal.model.OmgjoeringskravUtfall
import java.time.LocalDateTime
import java.util.UUID

object KabalTestdata {

    private val faker = Faker()

    fun lagKlagebehandlingAvsluttetEvent(
        kildeReferanse: String,
        utfall: KlageUtfall = KlageUtfall.entries.random(),
    ): BehandlingEvent = BehandlingEvent(
        eventId = UUID.randomUUID(),
        kildeReferanse = kildeReferanse,
        kilde = "SUPERHELT",
        kabalReferanse = UUID.randomUUID().toString(),
        type = BehandlingEventType.KLAGEBEHANDLING_AVSLUTTET,
        detaljer = BehandlingDetaljer(
            klagebehandlingAvsluttet = KlagebehandlingAvsluttetDetaljer(
                avsluttet = LocalDateTime.now(),
                utfall = utfall,
                journalpostReferanser = listOf(
                    "JP-${faker.number().digits(9)}",
                    "JP-${faker.number().digits(9)}",
                ),
            )
        ),
    )

    fun lagAnkebehandlingOpprettetEvent(
        kildeReferanse: String,
    ): BehandlingEvent = BehandlingEvent(
        eventId = UUID.randomUUID(),
        kildeReferanse = kildeReferanse,
        kilde = "SUPERHELT",
        kabalReferanse = UUID.randomUUID().toString(),
        type = BehandlingEventType.ANKEBEHANDLING_OPPRETTET,
        detaljer = BehandlingDetaljer(
            ankebehandlingOpprettet = AnkebehandlingOpprettetDetaljer(
                mottattKlageinstans = LocalDateTime.now(),
            )
        ),
    )

    fun lagAnkebehandlingAvsluttetEvent(
        kildeReferanse: String,
        utfall: AnkeUtfall = AnkeUtfall.entries.random(),
    ): BehandlingEvent = BehandlingEvent(
        eventId = UUID.randomUUID(),
        kildeReferanse = kildeReferanse,
        kilde = "SUPERHELT",
        kabalReferanse = UUID.randomUUID().toString(),
        type = BehandlingEventType.ANKEBEHANDLING_AVSLUTTET,
        detaljer = BehandlingDetaljer(
            ankebehandlingAvsluttet = AnkebehandlingAvsluttetDetaljer(
                avsluttet = LocalDateTime.now(),
                utfall = utfall,
                journalpostReferanser = listOf(
                    "JP-${faker.number().digits(9)}",
                ),
            )
        ),
    )

    fun lagAnkeITrygderettenOpprettetEvent(
        kildeReferanse: String,
    ): BehandlingEvent = BehandlingEvent(
        eventId = UUID.randomUUID(),
        kildeReferanse = kildeReferanse,
        kilde = "SUPERHELT",
        kabalReferanse = UUID.randomUUID().toString(),
        type = BehandlingEventType.ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET,
        detaljer = BehandlingDetaljer(
            ankeITrygderettenbehandlingOpprettet = AnkeITrygderettenbehandlingOpprettetDetaljer(
                sendtTilTrygderetten = LocalDateTime.now(),
            )
        ),
    )

    fun lagBehandlingFeilregistrertEvent(
        kildeReferanse: String,
        type: FeilregistrertBehandlingType = FeilregistrertBehandlingType.KLAGE,
        navIdent: String = "Z999999",
        reason: String = "Feilregistrert under testing",
    ): BehandlingEvent = BehandlingEvent(
        eventId = UUID.randomUUID(),
        kildeReferanse = kildeReferanse,
        kilde = "SUPERHELT",
        kabalReferanse = UUID.randomUUID().toString(),
        type = BehandlingEventType.BEHANDLING_FEILREGISTRERT,
        detaljer = BehandlingDetaljer(
            behandlingFeilregistrert = BehandlingFeilregistrertDetaljer(
                feilregistrert = LocalDateTime.now(),
                navIdent = navIdent,
                reason = reason,
                type = type,
            )
        ),
    )

    fun lagBehandlingEtterTrygderettenOpphevetAvsluttetEvent(
        kildeReferanse: String,
        utfall: KlageUtfall = KlageUtfall.entries.random(),
    ): BehandlingEvent = BehandlingEvent(
        eventId = UUID.randomUUID(),
        kildeReferanse = kildeReferanse,
        kilde = "SUPERHELT",
        kabalReferanse = UUID.randomUUID().toString(),
        type = BehandlingEventType.BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET,
        detaljer = BehandlingDetaljer(
            behandlingEtterTrygderettenOpphevetAvsluttet = BehandlingEtterTrygderettenOpphevetAvsluttetDetaljer(
                avsluttet = LocalDateTime.now(),
                utfall = utfall,
                journalpostReferanser = listOf(
                    "JP-${faker.number().digits(9)}",
                ),
            )
        ),
    )

    fun lagOmgjoeringskravbehandlingAvsluttetEvent(
        kildeReferanse: String,
        utfall: OmgjoeringskravUtfall = OmgjoeringskravUtfall.entries.random(),
    ): BehandlingEvent = BehandlingEvent(
        eventId = UUID.randomUUID(),
        kildeReferanse = kildeReferanse,
        kilde = "SUPERHELT",
        kabalReferanse = UUID.randomUUID().toString(),
        type = BehandlingEventType.OMGJOERINGSKRAVBEHANDLING_AVSLUTTET,
        detaljer = BehandlingDetaljer(
            omgjoeringskravbehandlingAvsluttet = OmgjoeringskravbehandlingAvsluttetDetaljer(
                avsluttet = LocalDateTime.now(),
                utfall = utfall,
                journalpostReferanser = listOf(
                    "JP-${faker.number().digits(9)}",
                ),
            )
        ),
    )

    fun lagGjenopptaksbehandlingAvsluttetEvent(
        kildeReferanse: String,
        utfall: GjenopptaksUtfall = GjenopptaksUtfall.entries.random(),
    ): BehandlingEvent = BehandlingEvent(
        eventId = UUID.randomUUID(),
        kildeReferanse = kildeReferanse,
        kilde = "SUPERHELT",
        kabalReferanse = UUID.randomUUID().toString(),
        type = BehandlingEventType.GJENOPPTAKSBEHANDLING_AVSLUTTET,
        detaljer = BehandlingDetaljer(
            gjenopptaksbehandlingAvsluttet = GjenopptaksbehandlingAvsluttetDetaljer(
                avsluttet = LocalDateTime.now(),
                utfall = utfall,
                journalpostReferanser = listOf(
                    "JP-${faker.number().digits(9)}",
                ),
            )
        ),
    )
}
