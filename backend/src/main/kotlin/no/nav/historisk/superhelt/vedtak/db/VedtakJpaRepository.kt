package no.nav.historisk.superhelt.vedtak.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VedtakJpaRepository : JpaRepository<VedtakJpaEntity, Long> {

    fun findBySak_Id(sakId: Long): List<VedtakJpaEntity>
}
