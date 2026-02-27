package no.nav.historisk.superhelt.utbetaling

import no.nav.helved.UtbetalingUuid
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.utbetaling.db.UtbetalingJpaEntity
import no.nav.historisk.superhelt.utbetaling.db.UtbetalingJpaRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Repository
class UtbetalingRepository(
    private val utbetalingJpaRepository: UtbetalingJpaRepository,
    private val sakRepository: SakRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun findActiveByBehandling(sak: Sak): Utbetaling? {
        val utbetalinger =
            utbetalingJpaRepository.findBySakIdAndBehandlingsnummer(sak.saksnummer.id, sak.behandlingsnummer)
                .sortedByDescending { it.utbetalingTidspunkt }
        return (utbetalinger.firstOrNull { !it.utbetalingStatus.isFinal() }
            ?: utbetalinger.lastOrNull())?.toDomain()
    }

    fun findByTransaksjonsId(transaksjonsId: UUID): Utbetaling? {
        return utbetalingJpaRepository.findByTransaksjonsId(transaksjonsId)?.toDomain()
    }

    fun findUtbetalingerFeilet(): List<Utbetaling> {
        return utbetalingJpaRepository.findByUtbetalingStatus(UtbetalingStatus.FEILET)
            .map { it.toDomain() }
    }

    fun opprettUtbetaling(sak: Sak): Utbetaling {
        val belop = sak.belop?.value ?: throw IllegalArgumentException("Beløp er ikke satt for sak ${sak.saksnummer}")
        var utbetalingsUuid = UtbetalingUuid.random()
        if (sak.gjenapnet) {
            logger.debug("Finner tidligere utbetaling og setter uuid lik")
            val tidligereUtbetaling = utbetalingJpaRepository.findBySakId(sak.saksnummer.id)
                .sortedByDescending { it.utbetalingTidspunkt }
                .firstOrNull { it.utbetalingStatus.isFinal() }
            utbetalingsUuid = tidligereUtbetaling?.utbetalingsUuid ?: UtbetalingUuid.random()
        }

        val sakEntity = sakRepository.getSakEntityOrThrow(sak.saksnummer)
        val entity = UtbetalingJpaEntity(
            sak = sakEntity,
            behandlingsnummer = sak.behandlingsnummer,
            belop = belop,
            utbetalingsUuid = utbetalingsUuid
        )
        logger.info("Oppretter utbetaling med transaksjonsId ${entity.transaksjonsId} og utbetalingsUuid ${entity.utbetalingsUuid} for sak ${sak.saksnummer} behandling ${sak.behandlingsnummer} med beløp $belop")
        return utbetalingJpaRepository.save(entity).toDomain()
    }

    @Transactional
    internal fun setUtbetalingStatus(transaksjonsId: UUID, status: UtbetalingStatus) {
        updateUtbetalingStatus(transaksjonsId, status)
    }

    internal fun setUtbetalingStatusSendt(transaksjonsId: UUID) {
        updateUtbetalingStatus(transaksjonsId, UtbetalingStatus.SENDT_TIL_UTBETALING, Instant.now())
    }

    private fun updateUtbetalingStatus(transaksjonsId: UUID, status: UtbetalingStatus, tidspunkt: Instant? = null) {
        utbetalingJpaRepository.findByTransaksjonsId(transaksjonsId)?.let {
            it.utbetalingStatus = status
            if (tidspunkt != null) {
                it.utbetalingTidspunkt = tidspunkt
            }
            utbetalingJpaRepository.save(it)
        }
    }

}
