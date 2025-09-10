package no.nav.historisk.superhelt.sak.model

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SakRepository : JpaRepository<SakEntity, Long> {
    fun findBySaksnummer(saksnummer: Saksnummer): SakEntity?

    @Query(value = "SELECT nextval(saksnummer_sequence)", nativeQuery = true)
    fun hentNesteSaksnummer(): Long
}