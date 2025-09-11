package no.nav.historisk.superhelt.sak.model

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SakRepository : JpaRepository<SakEntity, Long> {

}