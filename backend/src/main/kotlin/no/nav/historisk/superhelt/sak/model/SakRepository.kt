package no.nav.historisk.superhelt.sak.model

import no.nav.person.Fnr
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SakRepository : JpaRepository<SakEntity, Long> {

    fun findSakEntitiesByFnr(fnr: Fnr): List<SakEntity>

}