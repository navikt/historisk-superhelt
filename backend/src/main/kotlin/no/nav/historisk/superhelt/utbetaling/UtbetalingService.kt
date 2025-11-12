package no.nav.historisk.superhelt.utbetaling

import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.utbetaling.kafka.UtbetalingKafkaProducer
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UtbetalingService(
    private val utbetalingRepository: UtbetalingRepository,
    private val utbetalingKafkaProducer: UtbetalingKafkaProducer
) {

    private final val logger = LoggerFactory.getLogger(javaClass)

    @PreAuthorize("hasAuthority('WRITE')")
    fun sendTilUtbetaling(sak: Sak) {
        val utbetaling = sak.utbetaling
        utbetaling?.let { utbetaling ->

            if (utbetaling.utbetalingStatus !in listOf(UtbetalingStatus.UTKAST, UtbetalingStatus.KLAR_TIL_UTBETALING)) {
                throw IllegalStateException("Utbetaling med uuid ${utbetaling.uuid} er i status ${utbetaling.utbetalingStatus} og kan derfor ikke sendes til utbetaling")
            }
            // Setter først status i egen transaksjon
            utbetalingRepository.setUtbetalingStatus(utbetaling.uuid, UtbetalingStatus.KLAR_TIL_UTBETALING)
            //Ny transaksjon for å sende til kafka og oppdatere databasen med ny status
            utbetalingKafkaProducer.sendTilUtbetaling(sak, utbetaling)

        }
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

        if (!currentStatus.isAfter(newStatus)) {
            logger.debug(
                "Utbetaling {} er allerede i samme eller nyere status {}, {}. Ignoring update",
                utbetalingsId,
                currentStatus,
                newStatus
            )
            return
        }


        utbetalingRepository.setUtbetalingStatus(uuid = utbetalingsId, status = newStatus)
    }


}
