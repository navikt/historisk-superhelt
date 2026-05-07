package no.nav.historisk.superhelt.klage.db

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "kabal_event")
class KabalEventEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    /** Unik event-id fra Kabal – brukes til idempotens-sjekk */
    @Column(name = "event_id", nullable = false, unique = true)
    val eventId: UUID,

    @Column(name = "saksnummer", nullable = false)
    val saksnummer: String,

    @Column(name = "event_type", nullable = false)
    val eventType: String,

    @Column(name = "utfall")
    val utfall: String? = null,

    @Column(name = "tidspunkt", nullable = false)
    val tidspunkt: Instant,

    @Column(name = "aarsak_feilregistrert")
    val aarsakFeilregistrert: String? = null,

    @Column(name = "journalpost_referanser")
    val journalpostReferanser: String? = null,

    @Column(name = "opprettet_tid", nullable = false)
    val opprettetTid: Instant = Instant.now(),
)

