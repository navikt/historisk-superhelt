package no.nav.historisk.superhelt.utbetaling

import no.nav.historisk.superhelt.utbetaling.db.UtbetalingJpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
class UtbetalingRepository(private val utbetalingJpaRepository: UtbetalingJpaRepository) {


    internal fun oppdaterUtbetalingStatus(uuid: UUID, status: UtbetalingStatus) {
        utbetalingJpaRepository.findByUuid(uuid)?.let {
            it.utbetalingStatus = status
            if (status == UtbetalingStatus.SENDT_TIL_UTBETALING) {
                it.utbetalingTidspunkt = Instant.now()
            }
            utbetalingJpaRepository.save(it)
        }
    }
}
