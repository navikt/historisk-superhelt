package no.nav.historisk.superhelt.utbetaling

import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.utbetaling.UtbetalingSakExtentions.newUtbetaling
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
        return utbetalingJpaRepository.findBySakIdAndBehandlingsnummer(sak.saksnummer.id, sak.behandlingsnummer)
            ?.toDomain()
    }

    fun findBySak(saksnummer: Saksnummer): List<Utbetaling> {
        return utbetalingJpaRepository.findBySakId(saksnummer.id).map { it.toDomain() }
    }

    fun findByTransaksjonsId(transaksjonsId: UUID): Utbetaling? {
        return utbetalingJpaRepository.findByTransaksjonsId(transaksjonsId)?.toDomain()
    }

    fun findUtbetalingerFeilet(): List<Utbetaling> {
        return utbetalingJpaRepository.findByUtbetalingStatus(UtbetalingStatus.FEILET)
            .map { it.toDomain() }
    }

    fun lagreUtbetaling(utbetaling: Utbetaling): Utbetaling {
        val sakEntity = sakRepository.getSakEntityOrThrow(utbetaling.saksnummer)

        val entity = UtbetalingJpaEntity(
            sak = sakEntity,
            behandlingsnummer = sakEntity.behandlingsnummer,
            belop = utbetaling.belop.value,
            utbetalingsUuid = utbetaling.utbetalingsUuid,
            utbetalingStatus = utbetaling.utbetalingStatus,
            transaksjonsId = utbetaling.transaksjonsId,
//            utbetalingTidspunkt = utbetaling.utbetalingTidspunkt ?: Instant.now(),
        )
        logger.info("Oppretter utbetaling med transaksjonsId ${entity.transaksjonsId} og utbetalingsUuid ${entity.utbetalingsUuid} for sak ${sakEntity.saksnummer} behandling ${utbetaling.behandlingsnummer} med beløp ${utbetaling.belop}")
        return utbetalingJpaRepository.save(entity).toDomain()
    }

    fun opprettUtbetaling(sak: Sak, tidligereUtbetaling: Utbetaling? = null): Utbetaling {
        return lagreUtbetaling(sak.newUtbetaling(tidligereUtbetaling))
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
