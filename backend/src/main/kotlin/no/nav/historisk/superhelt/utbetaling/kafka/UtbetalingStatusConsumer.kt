package no.nav.historisk.superhelt.utbetaling.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.helved.StatusType
import no.nav.helved.UtbetalingStatusMelding
import no.nav.historisk.superhelt.utbetaling.Utbetaling
import no.nav.historisk.superhelt.utbetaling.UtbetalingRepository
import no.nav.historisk.superhelt.utbetaling.UtbetalingService
import no.nav.historisk.superhelt.utbetaling.UtbetalingStatus
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import java.util.*

@Service
class UtbetalingStatusConsumer(
    properties: UtbetalingConfigProperties,
    private val utbetalingRepository: UtbetalingRepository,
    private val utbetalingService: UtbetalingService,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    init {
        logger.info("Starter utbetaling status consumer for topic: ${properties.statusTopic}")
    }

    @KafkaListener(
        topics = ["\${app.utbetaling.status-topic}"],
        groupId = "historisk.superhelt.utbetaling.status",
        filter = "helvedStatusFagsystemHeaderFilter",
    )
    fun statusMessage(record: ConsumerRecord<String, String>) {
        val utbetalingsId = UUID.fromString(record.key())
        val utbetaling = utbetalingRepository.findByUuid(utbetalingsId)
        if (utbetaling == null) {
            logger.warn("Fant ikke utbetaling med id: {}. Ignoring message", utbetalingsId)
            return
        }
        logger.debug("Mottatt melding på topic: ${record.topic()} med key: ${record.key()} og value: ${record.value()}")
        val statusMessage = objectMapper.readValue(record.value(), UtbetalingStatusMelding::class.java)
        val newStatus = calculateNewStatus(utbetaling = utbetaling, statusMessage = statusMessage)
        utbetalingService.updateUtbetalingsStatus(utbetaling, newStatus)
    }

    private fun calculateNewStatus(
        utbetaling: Utbetaling,
        statusMessage: UtbetalingStatusMelding): UtbetalingStatus {
        val utbetalingsId = utbetaling.uuid

        return when (statusMessage.status) {
            StatusType.OK -> UtbetalingStatus.UTBETALT
            StatusType.FEILET -> {
                logger.error("Feilet {} med status {}", utbetalingsId, statusMessage)
                UtbetalingStatus.FEILET
            }

            StatusType.MOTTATT -> UtbetalingStatus.MOTTATT_AV_UTBETALING
            StatusType.HOS_OPPDRAG -> UtbetalingStatus.BEHANDLET_AV_UTBETALING
        }
    }

}
