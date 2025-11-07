package no.nav.historisk.superhelt.utbetaling

import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.utbetaling.kafka.UtbetalingKafkaProducer
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class UtbetalingService(
    private val utbetalingRepository: UtbetalingRepository,
    private val utbetalingKafkaProducer: UtbetalingKafkaProducer
) {


    @PreAuthorize("hasAuthority('WRITE')")
    fun sendTilUtbetaling(sak: Sak) {
        val utbetaling = sak.utbetaling
        utbetaling?.let { utbetaling ->

            if (utbetaling.utbetalingStatus !in listOf(UtbetalingStatus.UTKAST)) {
                throw IllegalStateException("Utbetaling med uuid ${utbetaling.uuid} er i status ${utbetaling.utbetalingStatus} og kan derfor ikke sendes til utbetaling")
            }
            // Setter først status i egen transaksjon
            utbetalingRepository.setUtbetalingStatusKlar(utbetaling.uuid)
            //Ny transaksjon for å sende til kafka og oppdatere databasen med ny status
            utbetalingKafkaProducer.sendTilUtbetaling(sak, utbetaling)

        }
    }


}
