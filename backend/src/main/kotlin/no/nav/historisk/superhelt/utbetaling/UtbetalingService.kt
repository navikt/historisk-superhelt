package no.nav.historisk.superhelt.utbetaling

import no.nav.historisk.superhelt.sak.Utbetaling
import no.nav.historisk.superhelt.sak.db.UtbetalingJpaEntity
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class UtbetalingService(private val kafkaTemplate: KafkaTemplate<String, String>) {

    fun sendTilUtbetaling(utbetaling: Utbetaling) {
        val result = kafkaTemplate.send("superhelt-utbetalinger", "Utbetaling  yay")
            .get()
        println("Melding sendt til topic superhelt-utbetalinger ${result}")
    }

}