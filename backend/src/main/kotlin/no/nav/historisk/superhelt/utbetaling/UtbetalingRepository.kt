package no.nav.historisk.superhelt.utbetaling

import no.nav.common.types.Belop
import no.nav.common.types.Saksnummer
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

    private fun lagreUtbetaling(
        saksnummer: Saksnummer,
        belop: Belop,
        utbetalingUuid: UtbetalingUuid? = null,
        utbetalingStatus: UtbetalingStatus = UtbetalingStatus.UTKAST,
        utbetalingTidspunkt: Instant,
    ): Utbetaling {
        val sakEntity = sakRepository.getSakEntityOrThrow(saksnummer)

        val entity = UtbetalingJpaEntity(
            sak = sakEntity,
            behandlingsnummer = sakEntity.behandlingsnummer,
            belop = belop.value,
            utbetalingsUuid = utbetalingUuid ?: UtbetalingUuid.random(),
            utbetalingStatus = utbetalingStatus,
            transaksjonsId = UUID.randomUUID(),
            utbetalingTidspunkt = utbetalingTidspunkt
        )
        val utbetaling = utbetalingJpaRepository.save(entity).toDomain()
        logger.info("Oppretter utbetaling med ${utbetaling.loggId} med beløp $belop")
        return utbetaling
    }

    fun opprettAnnullering(sak: Sak, tidligereUtbetaling: Utbetaling? = null): Utbetaling {
        return lagreUtbetaling(
            saksnummer = sak.saksnummer,
            belop = Belop.ZeroBelop,
            utbetalingUuid = tidligereUtbetaling?.utbetalingsUuid,
            utbetalingTidspunkt = tidligereUtbetaling?.utbetalingTidspunkt ?: Instant.now()
        )
    }

    fun opprettUtbetaling(sak: Sak, tidligereUtbetaling: Utbetaling? = null): Utbetaling {
        return lagreUtbetaling(
            saksnummer = sak.saksnummer,
            belop = sak.belop
                ?: throw IllegalStateException("Sak ${sak.saksnummer} har ingen beløp, kan ikke opprette utbetaling"),
            utbetalingUuid = tidligereUtbetaling?.utbetalingsUuid,
            utbetalingTidspunkt = tidligereUtbetaling?.utbetalingTidspunkt ?: Instant.now()
        )
    }

    @Transactional
    internal fun setUtbetalingStatus(transaksjonsId: UUID, status: UtbetalingStatus) {
        updateUtbetalingStatus(transaksjonsId, status)
    }

    internal fun setUtbetalingStatusSendt(transaksjonsId: UUID) {
        updateUtbetalingStatus(transaksjonsId, UtbetalingStatus.SENDT_TIL_UTBETALING)
    }

    private fun updateUtbetalingStatus(transaksjonsId: UUID, status: UtbetalingStatus) {
        utbetalingJpaRepository.findByTransaksjonsId(transaksjonsId)?.let {
            if (it.utbetalingTidspunkt == null) {
                it.utbetalingTidspunkt = Instant.now()
            }
            it.utbetalingStatus = status
            utbetalingJpaRepository.save(it)
        }
    }

}
