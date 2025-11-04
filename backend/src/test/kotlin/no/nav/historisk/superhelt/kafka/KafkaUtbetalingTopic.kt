package no.nav.historisk.superhelt.kafka

import org.apache.kafka.clients.admin.NewTopic
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/** Lager en Kafka-topic for utbetalinger i superhelt-systemet. for test only */
@Component
class KafkaUtbetalingTopic : NewTopic(
    "superhelt-utbetalinger",
    1,
    1.toShort()
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    init {
        logger.info("Kafka topic initiated")
    }

}