package no.nav.historisk.superhelt.klage.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface KlageJpaRepository : JpaRepository<KlageJpaEntity, UUID> {

    fun findBySakId(sakId: Long): List<KlageJpaEntity>

    fun findByKabalBehandlingId(kabalBehandlingId: String): KlageJpaEntity?
}

