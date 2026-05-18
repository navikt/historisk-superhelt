package no.nav.historisk.mock.kabal

import net.datafaker.Faker
import no.nav.kabal.model.AnkeITrygderettenbehandlingOpprettetDetaljer
import no.nav.kabal.model.AnkeUtfall
import no.nav.kabal.model.AnkebehandlingAvsluttetDetaljer
import no.nav.kabal.model.AnkebehandlingOpprettetDetaljer
import no.nav.kabal.model.BehandlingEtterTrygderettenOpphevetAvsluttetDetaljer
import no.nav.kabal.model.BehandlingFeilregistrertDetaljer
import no.nav.kabal.model.FeilregistrertBehandlingType
import no.nav.kabal.model.GjenopptaksUtfall
import no.nav.kabal.model.GjenopptaksbehandlingAvsluttetDetaljer
import no.nav.kabal.model.KabalBehandlingDetaljer
import no.nav.kabal.model.KabalBehandlingEvent
import no.nav.kabal.model.KabalBehandlingEventType
import no.nav.kabal.model.KlageUtfall
import no.nav.kabal.model.KlagebehandlingAvsluttetDetaljer
import no.nav.kabal.model.OmgjoeringskravUtfall
import no.nav.kabal.model.OmgjoeringskravbehandlingAvsluttetDetaljer
import java.time.LocalDateTime
import java.util.UUID

object KabalTestdata {

    private val faker = Faker()

    fun lagKlagebehandlingAvsluttetEvent(
        kildeReferanse: String,
        utfall: KlageUtfall = KlageUtfall.entries.random(),
    ): KabalBehandlingEvent = KabalBehandlingEvent(
        eventId = UUID.randomUUID(),
        kildeReferanse = kildeReferanse,
        kilde = "SUPERHELT",
        kabalReferanse = UUID.randomUUID().toString(),
        type = KabalBehandlingEventType.KLAGEBEHANDLING_AVSLUTTET,
        detaljer = KabalBehandlingDetaljer(
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
    ): KabalBehandlingEvent = KabalBehandlingEvent(
        eventId = UUID.randomUUID(),
        kildeReferanse = kildeReferanse,
        kilde = "SUPERHELT",
        kabalReferanse = UUID.randomUUID().toString(),
        type = KabalBehandlingEventType.ANKEBEHANDLING_OPPRETTET,
        detaljer = KabalBehandlingDetaljer(
            ankebehandlingOpprettet = AnkebehandlingOpprettetDetaljer(
                mottattKlageinstans = LocalDateTime.now(),
            )
        ),
    )

    fun lagAnkebehandlingAvsluttetEvent(
        kildeReferanse: String,
        utfall: AnkeUtfall = AnkeUtfall.entries.random(),
    ): KabalBehandlingEvent = KabalBehandlingEvent(
        eventId = UUID.randomUUID(),
        kildeReferanse = kildeReferanse,
        kilde = "SUPERHELT",
        kabalReferanse = UUID.randomUUID().toString(),
        type = KabalBehandlingEventType.ANKEBEHANDLING_AVSLUTTET,
        detaljer = KabalBehandlingDetaljer(
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
    ): KabalBehandlingEvent = KabalBehandlingEvent(
        eventId = UUID.randomUUID(),
        kildeReferanse = kildeReferanse,
        kilde = "SUPERHELT",
        kabalReferanse = UUID.randomUUID().toString(),
        type = KabalBehandlingEventType.ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET,
        detaljer = KabalBehandlingDetaljer(
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
    ): KabalBehandlingEvent = KabalBehandlingEvent(
        eventId = UUID.randomUUID(),
        kildeReferanse = kildeReferanse,
        kilde = "SUPERHELT",
        kabalReferanse = UUID.randomUUID().toString(),
        type = KabalBehandlingEventType.BEHANDLING_FEILREGISTRERT,
        detaljer = KabalBehandlingDetaljer(
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
    ): KabalBehandlingEvent = KabalBehandlingEvent(
        eventId = UUID.randomUUID(),
        kildeReferanse = kildeReferanse,
        kilde = "SUPERHELT",
        kabalReferanse = UUID.randomUUID().toString(),
        type = KabalBehandlingEventType.BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET,
        detaljer = KabalBehandlingDetaljer(
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
    ): KabalBehandlingEvent = KabalBehandlingEvent(
        eventId = UUID.randomUUID(),
        kildeReferanse = kildeReferanse,
        kilde = "SUPERHELT",
        kabalReferanse = UUID.randomUUID().toString(),
        type = KabalBehandlingEventType.OMGJOERINGSKRAVBEHANDLING_AVSLUTTET,
        detaljer = KabalBehandlingDetaljer(
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
    ): KabalBehandlingEvent = KabalBehandlingEvent(
        eventId = UUID.randomUUID(),
        kildeReferanse = kildeReferanse,
        kilde = "SUPERHELT",
        kabalReferanse = UUID.randomUUID().toString(),
        type = KabalBehandlingEventType.GJENOPPTAKSBEHANDLING_AVSLUTTET,
        detaljer = KabalBehandlingDetaljer(
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
