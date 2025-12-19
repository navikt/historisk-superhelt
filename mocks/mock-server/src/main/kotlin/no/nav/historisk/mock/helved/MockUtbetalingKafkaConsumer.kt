package no.nav.historisk.mock.helved

import no.nav.helved.StatusType
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import java.time.Instant


@Service
class MockUtbetalingKafkaConsumer(
    private val statusKafkaProducer: MockUtbetalingStatusKafkaProducer,
    private val objectMapper: ObjectMapper,
    private val taskScheduler: TaskScheduler
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

        taskScheduler.schedule(
            { statusKafkaProducer.sendStatusMessage(key, StatusType.HOS_OPPDRAG) },
            Instant.now().plusSeconds(2)
        )

        taskScheduler.schedule(
            { statusKafkaProducer.sendStatusMessage(key, StatusType.OK) },
            Instant.now().plusSeconds(30)
        )
    }

}
