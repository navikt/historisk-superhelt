package no.nav.historisk.mock.helved

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.helved.StatusType
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service


@Service
class MockUtbetalingKafkaConsumer(
    private val statusKafkaProducer: MockUtbetalingStatusKafkaProducer,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)


    @KafkaListener(
        topics = ["\${app.helved.utbetaling-topic}"],
        groupId = "mock-helved-utbetaling-consumer",
    )
    fun statusMessage(record: ConsumerRecord<String, String>) {
        val key = record.key()
        logger.info("Mottatt melding på topic: ${record.topic()} med key: $key og value: ${record.value()}")

        statusKafkaProducer.sendStatusMessage(key, StatusType.MOTTATT)
        logger.debug("Sendt MOTTATT status for utbetaling: $key")

        Thread.sleep(2000)
        statusKafkaProducer.sendStatusMessage(key, StatusType.HOS_OPPDRAG)
        logger.debug("Sendt HOS_OPPDRAG status for utbetaling: $key")

        Thread.sleep(10_000)
        statusKafkaProducer.sendStatusMessage(key, StatusType.OK)
        logger.debug("Sendt OK status for utbetaling: $key")
    }


}
