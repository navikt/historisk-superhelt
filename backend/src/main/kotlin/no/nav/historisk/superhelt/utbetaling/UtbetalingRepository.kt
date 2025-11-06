package no.nav.historisk.superhelt.utbetaling

import no.nav.historisk.superhelt.utbetaling.db.UtbetalingJpaRepository
import org.springframework.stereotype.Repository

@Repository
class UtbetalingRepository(private val utbetalingJpaRepository: UtbetalingJpaRepository) {
  fun saveOrUpdate(utbetaling: Utbetaling) {}
}
