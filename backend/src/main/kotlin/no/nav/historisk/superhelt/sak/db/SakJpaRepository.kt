package no.nav.historisk.superhelt.sak.db

import no.nav.person.Fnr
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SakJpaRepository : JpaRepository<SakJpaEntity, Long> {

    fun findSakEntitiesByFnr(fnr: Fnr): List<SakJpaEntity>

}