package no.nav.historisk.superhelt.utbetaling

import no.nav.historisk.superhelt.utbetaling.db.UtbetalingJpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Repository
class UtbetalingRepository(private val utbetalingJpaRepository: UtbetalingJpaRepository) {

    fun findByUuid(uuid: UUID): Utbetaling? {
        return utbetalingJpaRepository.findByUuid(uuid)?.toDomain()
    }

    @Transactional
    internal fun setUtbetalingStatus(uuid: UUID, status: UtbetalingStatus) {
        updateUtbetalingStatus(uuid, status)
    }

    internal fun setUtbetalingStatusSendt(uuid: UUID) {
        updateUtbetalingStatus(uuid, UtbetalingStatus.SENDT_TIL_UTBETALING, Instant.now())
    }

    private fun updateUtbetalingStatus(uuid: UUID, status: UtbetalingStatus, tidspunkt: Instant? = null) {
        utbetalingJpaRepository.findByUuid(uuid)?.let {
            it.utbetalingStatus = status
            if (tidspunkt != null) {
                it.utbetalingTidspunkt = tidspunkt
            }
            utbetalingJpaRepository.save(it)
        }
    }

}
