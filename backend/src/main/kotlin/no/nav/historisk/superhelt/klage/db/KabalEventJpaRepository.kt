package no.nav.historisk.superhelt.klage.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface KabalEventJpaRepository : JpaRepository<KabalEventEntity, UUID> {
    fun findBySaksnummerOrderByTidspunktDesc(saksnummer: String): List<KabalEventEntity>
    fun existsByEventId(eventId: UUID): Boolean
}
