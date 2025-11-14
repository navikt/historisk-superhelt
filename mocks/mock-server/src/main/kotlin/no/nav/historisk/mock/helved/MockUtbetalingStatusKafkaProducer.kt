package no.nav.historisk.mock.helved

import no.nav.helved.StatusType
import no.nav.helved.UtbetalingStatusMelding
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class MockUtbetalingStatusKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, UtbetalingStatusMelding>,
    properties: HelvedConfigProperties

) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val statusTopic = properties.statusTopic

    fun sendStatusMessage(id: String, status: StatusType) {
        val melding = HelvedTestdata.lagStatusMelding(
            utbetalingId = id,
            status = status,
        )
        sendStatusMessage(id, melding)

    }

    fun sendStatusMessage(
        id: String,
        melding: UtbetalingStatusMelding) {
        logger.debug(
            "Sender statusmelding til {} for utbetaling med id {} og status {}",
            statusTopic,
            id,
            melding.status
        )
        kafkaTemplate.send(statusTopic, id, melding).get()
    }

}
