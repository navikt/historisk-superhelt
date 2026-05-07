package no.nav.historisk.superhelt.klage.db

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface KabalEventJpaRepository : CrudRepository<KabalEventEntity, UUID> {
    fun existsByEventId(eventId: UUID): Boolean
    fun findBySaksnummerOrderByTidspunktDesc(saksnummer: String): List<KabalEventEntity>
}

