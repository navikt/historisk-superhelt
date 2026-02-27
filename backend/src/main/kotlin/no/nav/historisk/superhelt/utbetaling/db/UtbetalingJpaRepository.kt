package no.nav.historisk.superhelt.utbetaling.db

import no.nav.historisk.superhelt.utbetaling.UtbetalingStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UtbetalingJpaRepository : JpaRepository<UtbetalingJpaEntity, Long> {

    fun countByUtbetalingStatus(utbetalingStatus: UtbetalingStatus): Long

    fun findByUtbetalingStatus(utbetalingStatus: UtbetalingStatus): List<UtbetalingJpaEntity>

    fun findByUuid(uuid: UUID): UtbetalingJpaEntity?

    fun findBySakId(sakId: Long): List<UtbetalingJpaEntity>
}
