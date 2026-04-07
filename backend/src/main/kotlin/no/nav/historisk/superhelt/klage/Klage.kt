package no.nav.historisk.superhelt.klage
import no.nav.common.types.Saksnummer
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
data class Klage(
    val id: UUID,
    val saksnummer: Saksnummer,
    /** Hjemmel-ID sendt til Kabal, f.eks. "FTRL_10_7I" */
    val hjemmelId: String,
    /** Dato klagen vart mottatt av NAV */
    val datoKlageMottatt: LocalDate,
    val kommentar: String?,
    /** NAV-enhet til saksbehandlaren som sende klagen */
    val forrigeBehandlendeEnhet: String,
    val sendtTidspunkt: Instant,
    /** Oppdaterast via Kafka-consumer når Kabal returnerer svar */
    val status: KlageStatus,
)
