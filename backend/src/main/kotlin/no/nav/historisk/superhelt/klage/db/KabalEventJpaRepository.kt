package no.nav.historisk.superhelt.klage.db

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface KabalEventJpaRepository : CrudRepository<KabalEventEntity, UUID> {
    fun findBySaksnummerOrderByTidspunktDesc(saksnummer: String): List<KabalEventEntity>

    @Modifying
    @Query(
        value = """
            INSERT INTO kabal_event
                (id, event_id, saksnummer, event_type, utfall, tidspunkt,
                 aarsak_feilregistrert, journalpost_referanser, opprettet_tid)
            VALUES
                (:id, :eventId, :saksnummer, :eventType, :utfall, :tidspunkt,
                 :aarsakFeilregistrert, :journalpostReferanser, now())
            ON CONFLICT (event_id) DO NOTHING
        """,
        nativeQuery = true,
    )
    fun insertOnConflictDoNothing(
        id: UUID,
        eventId: UUID,
        saksnummer: String,
        eventType: String,
        utfall: String?,
        tidspunkt: Instant,
        aarsakFeilregistrert: String?,
        journalpostReferanser: String?,
    ): Int
}

