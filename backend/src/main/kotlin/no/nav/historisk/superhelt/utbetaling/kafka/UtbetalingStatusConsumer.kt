package no.nav.historisk.superhelt.utbetaling.kafka

import no.nav.historisk.superhelt.utbetaling.UtbetalingService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class UtbetalingStatusConsumer(
    properties: UtbetalingConfigProperties,
    private val utbetalingService: UtbetalingService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    init {
        logger.info("Starter utbetaling status consumer for topic: ${properties.statusTopic}")
    }

    @KafkaListener(
        topics = ["\${app.utbetaling.status-topic}"],
        groupId = "historisk.superhelt.utbetaling.status"
    )
    fun statusMessage(message: String) {
        logger.info("Mottatt melding på topic : $message")
        // lage json av meldingen
        // oppdatere utbetaling status i databasen
        
    }
}
