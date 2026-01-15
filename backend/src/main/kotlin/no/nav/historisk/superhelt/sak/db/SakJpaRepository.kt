package no.nav.historisk.superhelt.sak.db

import no.nav.common.types.FolkeregisterIdent
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.StonadsType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SakJpaRepository : JpaRepository<SakJpaEntity, Long> {
    fun findSakEntitiesByFnr(fnr: FolkeregisterIdent): List<SakJpaEntity>

    fun countByTypeAndStatus(type: StonadsType, status: SakStatus): Long
}
