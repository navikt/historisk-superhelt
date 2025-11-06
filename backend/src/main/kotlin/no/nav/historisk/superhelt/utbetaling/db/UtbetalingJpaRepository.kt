package no.nav.historisk.superhelt.utbetaling.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UtbetalingJpaRepository : JpaRepository<UtbetalingJpaEntity, Long> {

    fun findByUuid(uuid: UUID): UtbetalingJpaEntity?
}
