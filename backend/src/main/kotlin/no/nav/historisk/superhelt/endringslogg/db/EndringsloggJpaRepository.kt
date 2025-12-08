package no.nav.historisk.superhelt.endringslogg.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EndringsloggJpaRepository : JpaRepository<EndringsloggJpaEntity, Long> {

    fun findBySak_Id(sakId: Long): List<EndringsloggJpaEntity>
}
