package no.nav.historisk.superhelt.utbetaling

import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.utbetaling.kafka.UtbetalingKafkaProducer
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UtbetalingService(
    private val utbetalingRepository: UtbetalingRepository,
    private val utbetalingKafkaProducer: UtbetalingKafkaProducer
) {


    @PreAuthorize("hasAuthority('WRITE')")
    @Transactional
    fun sendTilUtbetaling(sak: Sak) {
        val utbetaling = sak.utbetaling
        utbetaling?.let { utbetaling ->
            if (utbetaling.utbetalingStatus != UtbetalingStatus.UTKAST) {
                throw IllegalStateException("Utbetaling med uuid ${utbetaling.uuid} er ikke i status Utkast og kan derfor ikke sendes til utbetaling")
            }
            utbetalingRepository.oppdaterUtbetalingStatus(utbetaling.uuid, UtbetalingStatus.KLAR_TIL_UTBETALING)
            // Synkron for nå kan vurdere å gjøre asynkront senere
            utbetalingKafkaProducer.sendTilUtbetaling(sak, utbetaling)
            utbetalingRepository.oppdaterUtbetalingStatus(utbetaling.uuid, UtbetalingStatus.SENDT_TIL_UTBETALING)
        }
    }


}
