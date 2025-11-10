package no.nav.historisk.superhelt.kafka

import no.nav.historisk.superhelt.utbetaling.kafka.UtbetalingConfigProperties
import org.apache.kafka.clients.admin.NewTopic
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/** Lager en Kafka-topic for utbetalinger i superhelt-systemet. for test only */
@Component
class KafkaUtbetalingStatusTopic(utbetalingConfigProperties: UtbetalingConfigProperties) :
  NewTopic(utbetalingConfigProperties.statusTopic, 1, 1.toShort()) {
  private val logger = LoggerFactory.getLogger(this::class.java)

  init {
    logger.info("Kafka topic initiated " + utbetalingConfigProperties.statusTopic)
  }
}
