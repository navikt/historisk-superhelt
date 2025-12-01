package no.nav.historisk.superhelt.brev.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BrevJpaRepository : JpaRepository<BrevutkastJpaEntity, Long> {
    fun findAllBySakId(sakId: Long) : List<BrevutkastJpaEntity>
    fun findByUuid(uuid: UUID): BrevutkastJpaEntity?
}
