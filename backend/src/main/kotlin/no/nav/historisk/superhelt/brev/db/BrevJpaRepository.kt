package no.nav.historisk.superhelt.brev.db

import no.nav.historisk.superhelt.brev.BrevId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BrevJpaRepository : JpaRepository<BrevutkastJpaEntity, Long> {
    fun findAllBySakId(sakId: Long) : List<BrevutkastJpaEntity>
    fun findByUuid(uuid: BrevId): BrevutkastJpaEntity?
}
