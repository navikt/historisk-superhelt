package no.nav.historisk.superhelt.utbetaling

import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.sak.db.SakJpaRepository
import no.nav.historisk.superhelt.utbetaling.db.UtbetalingJpaEntity
import no.nav.historisk.superhelt.utbetaling.db.UtbetalingJpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Repository
class UtbetalingRepository(
    private val utbetalingJpaRepository: UtbetalingJpaRepository,
    private val sakJpaRepository: SakJpaRepository,
) {

    fun findActiveBySaksnummer(saksnummer: Saksnummer): Utbetaling? {
        val utbetalinger = utbetalingJpaRepository.findBySakId(saksnummer.id)
            .sortedByDescending { it.utbetalingTidspunkt }
        return (utbetalinger.firstOrNull { !it.utbetalingStatus.isFinal() }
            ?: utbetalinger.lastOrNull())?.toDomain()
    }

    fun findByUuid(uuid: UUID): Utbetaling? {
        return utbetalingJpaRepository.findByUuid(uuid)?.toDomain()
    }

    fun findUtbetalingerFeilet(): List<Utbetaling> {
        return utbetalingJpaRepository.findByUtbetalingStatus(UtbetalingStatus.FEILET)
            .map { it.toDomain() }
    }

    fun opprettUtbetaling(saksnummer: Saksnummer, belop: Int): Utbetaling {
        val sakEntity = sakJpaRepository.findByIdOrNull(saksnummer.id)
            ?: throw IllegalStateException("Sak ${saksnummer} ikke funnet")
        val entity = UtbetalingJpaEntity(sak = sakEntity, belop = belop)
        return utbetalingJpaRepository.save(entity).toDomain()
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
