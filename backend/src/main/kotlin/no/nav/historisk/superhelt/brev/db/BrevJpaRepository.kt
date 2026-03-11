package no.nav.historisk.superhelt.brev.db

import no.nav.historisk.superhelt.brev.BrevId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface BrevJpaRepository : JpaRepository<BrevJpaEntity, Long> {
    fun findAllBySakId(sakId: Long): List<BrevJpaEntity>
    fun findByUuid(uuid: BrevId): BrevJpaEntity?

    @Modifying
    @Query("DELETE FROM BrevJpaEntity b WHERE b.uuid = :uuid")
    fun deleteByUuid(uuid: BrevId)
}
