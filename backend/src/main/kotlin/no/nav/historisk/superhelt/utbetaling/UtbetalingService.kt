package no.nav.historisk.superhelt.utbetaling

import no.nav.historisk.superhelt.endringslogg.EndringsloggService
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.utbetaling.kafka.UtbetalingKafkaProducer
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UtbetalingService(
    private val utbetalingRepository: UtbetalingRepository,
    private val utbetalingKafkaProducer: UtbetalingKafkaProducer,
    private val sakEndringsloggService: EndringsloggService,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @PreAuthorize("hasAuthority('WRITE')")
    @Transactional
    fun sendTilUtbetaling(sak: Sak) {
        if (sak.utbetalingsType != UtbetalingsType.BRUKER) {
            logger.info("Sak ${sak.saksnummer} har utbetalingsType ${sak.utbetalingsType}, ingen utbetaling opprettes")
            return
        }
        // finne utbetaling for sak og behandling. Opprertt ny hvis det trengs
        val utbetaling = utbetalingRepository.findActiveByBehandling(sak)
            ?: run {
                val belop = sak.belop ?: throw IllegalStateException("Beløp er ikke satt for sak ${sak.saksnummer}")
                utbetalingRepository.opprettUtbetaling(sak)
            }
        if (utbetaling.utbetalingStatus !in listOf(UtbetalingStatus.UTKAST, UtbetalingStatus.KLAR_TIL_UTBETALING)) {
            logger.info("Utbetaling ${utbetaling.uuid} i sak ${sak.saksnummer} er i status ${utbetaling.utbetalingStatus} og vil ikke sendes på nytt til utbetaling")
            return
        }
        utbetalingRepository.setUtbetalingStatus(utbetaling.uuid, UtbetalingStatus.KLAR_TIL_UTBETALING)
        utbetalingKafkaProducer.sendTilUtbetaling(sak, utbetaling)
    }

    @Transactional
    fun updateUtbetalingsStatus(utbetaling: Utbetaling, newStatus: UtbetalingStatus) {
        val utbetalingsId = utbetaling.uuid
        val currentStatus = utbetaling.utbetalingStatus

        if (currentStatus.isFinal()) {
            logger.info(
                "Utbetaling {} er i final status {}. Ignoring update til {}",
                utbetalingsId,
                currentStatus,
                newStatus
            )
            return
        }

        if (!currentStatus.shouldBeUpdatedTo(newStatus)) {
            logger.debug(
                "Utbetaling {} er allerede i samme eller nyere status {}, {}. Ignoring update",
                utbetalingsId,
                currentStatus,
                newStatus
            )
            return
        }

        if (newStatus.isFinal()) {
            when (newStatus) {
                UtbetalingStatus.UTBETALT -> sakEndringsloggService.logChange(
                    saksnummer = utbetaling.saksnummer,
                    endringsType = EndringsloggType.UTBETALING_OK,
                    endring = "Utbetalt ${utbetaling.belop} til bruker"
                )

                UtbetalingStatus.FEILET -> sakEndringsloggService.logChange(
                    saksnummer = utbetaling.saksnummer,
                    endringsType = EndringsloggType.UTBETALING_FEILET,
                    endring = "Utbetaling til bruker feilet"
                )

                else -> {}
            }
        }

        utbetalingRepository.setUtbetalingStatus(uuid = utbetalingsId, status = newStatus)
    }

    @PreAuthorize("hasAuthority('WRITE')")
    fun retryUtbetaling(sak: Sak) {
        val utbetaling = utbetalingRepository.findActiveByBehandling(sak)
            ?: throw IllegalStateException("Utbetaling er ikke funnet for sak ${sak.saksnummer}")
        if (utbetaling.utbetalingStatus != UtbetalingStatus.FEILET) {
            throw IllegalStateException("Utbetaling i sak ${sak.saksnummer} er i status ${utbetaling.utbetalingStatus} og kan derfor ikke kjøres på nytt")
        }
        utbetalingRepository.setUtbetalingStatus(utbetaling.uuid, UtbetalingStatus.KLAR_TIL_UTBETALING)
        sendTilUtbetaling(sak)
    }


}
