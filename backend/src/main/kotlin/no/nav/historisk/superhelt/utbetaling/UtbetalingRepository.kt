package no.nav.historisk.superhelt.utbetaling

import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.db.SakJpaRepository
import no.nav.historisk.superhelt.utbetaling.db.UtbetalingJpaEntity
import no.nav.historisk.superhelt.utbetaling.db.UtbetalingJpaRepository
import org.slf4j.LoggerFactory
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
    private val logger = LoggerFactory.getLogger(javaClass)

    fun findActiveByBehandling(sak: Sak): Utbetaling? {
        val utbetalinger = utbetalingJpaRepository.findBySakIdAndBehandlingsnummer(sak.saksnummer.id, sak.behandlingsnummer)
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

    fun opprettUtbetaling(sak: Sak): Utbetaling {
        val belop = sak.belop?.value ?: throw IllegalStateException("Beløp er ikke satt for sak ${sak.saksnummer}")
        val sakEntity = sakJpaRepository.findByIdOrNull(sak.saksnummer.id)
            ?: throw IllegalStateException("Sak ${sak.saksnummer} ikke funnet")
        val entity = UtbetalingJpaEntity(sak = sakEntity, behandlingsnummer = sak.behandlingsnummer, belop = belop)
        logger.info("Oppretter utbetaling med uuid ${entity.uuid} for sak ${sak.saksnummer} behandling ${sak.behandlingsnummer} med beløp $belop")
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
