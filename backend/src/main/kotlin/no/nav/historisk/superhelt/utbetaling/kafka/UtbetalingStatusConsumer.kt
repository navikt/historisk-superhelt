package no.nav.historisk.superhelt.utbetaling.kafka

import no.nav.helved.StatusType
import no.nav.helved.UtbetalingStatusMelding
import no.nav.historisk.superhelt.infrastruktur.authentication.Permission
import no.nav.historisk.superhelt.infrastruktur.authentication.SecurityContextUtils
import no.nav.historisk.superhelt.utbetaling.Utbetaling
import no.nav.historisk.superhelt.utbetaling.UtbetalingRepository
import no.nav.historisk.superhelt.utbetaling.UtbetalingService
import no.nav.historisk.superhelt.utbetaling.UtbetalingStatus
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
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
        val transaksjonsId = UUID.fromString(record.key())
        val utbetaling = utbetalingRepository.findByTransaksjonsId(transaksjonsId)
        if (utbetaling == null) {
            logger.warn("Fant ikke utbetaling med transaksjonsId: {}. Ignoring message", transaksjonsId)
            return
        }
        SecurityContextUtils.runAsSystemuser(
            name = "utbetaling-system",
            permissions = listOf(
                Permission.READ,
                Permission.WRITE,
                Permission.IGNORE_TILGANGSMASKIN
            )
        ) {
            val statusMessage = objectMapper.readValue(record.value(), UtbetalingStatusMelding::class.java)
            val newStatus = calculateNewStatus(utbetaling = utbetaling, statusMessage = statusMessage)
            val belop= statusMessage.detaljer?.linjer?.firstOrNull()?.beløp
            logger.debug(
                "Mottatt melding på topic: {} med key: {}. Ny status {} beløp utbetalt {} ",
                record.topic(),
                record.key(),
                newStatus,
                belop
            )
            //TODO hvis riktig sum kommer tilbake kan dette logges
            utbetalingService.updateUtbetalingsStatus(utbetaling, newStatus)
        }
    }

    private fun calculateNewStatus(
        utbetaling: Utbetaling,
        statusMessage: UtbetalingStatusMelding): UtbetalingStatus {
        val utbetalingsId = utbetaling.transaksjonsId

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
