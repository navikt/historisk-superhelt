package no.nav.historisk.superhelt.klage.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

interface KlageJpaRepository : JpaRepository<KlageJpaEntity, UUID> {
    fun findBySakId(sakId: Long): List<KlageJpaEntity>
}


