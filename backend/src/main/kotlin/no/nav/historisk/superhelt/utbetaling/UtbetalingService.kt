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

    //TODO sjekke tilgang på saken i stedet for write
    @PreAuthorize("hasAuthority('WRITE')")
    fun sendTilUtbetaling(sak: Sak) {
        sak.utbetaling?.let {
            val utbetaling = utbetalingRepository.findByUuid(it.uuid)
                ?: throw IllegalStateException("Utbetaling med uuid ${it.uuid} ikke funnet")

            if (utbetaling.utbetalingStatus !in listOf(UtbetalingStatus.UTKAST, UtbetalingStatus.KLAR_TIL_UTBETALING)) {
                logger.info("Utbetaling ${utbetaling.uuid} i sak ${sak.saksnummer} er i status ${utbetaling.utbetalingStatus} og vil ikke sendes på nytt til utbetaling")
                return
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
        val utbetaling = sak.utbetaling?: throw IllegalStateException("Utbetaling er ikke funnet")
        if (utbetaling.utbetalingStatus!= UtbetalingStatus.FEILET) {
            throw IllegalStateException("Utbetaling i sak ${sak.saksnummer} er i status ${utbetaling.utbetalingStatus} og kan derfor ikke kjøres på nytt")
        }
        utbetalingRepository.setUtbetalingStatus(utbetaling.uuid, UtbetalingStatus.KLAR_TIL_UTBETALING)
        sendTilUtbetaling(sak)
    }


}
