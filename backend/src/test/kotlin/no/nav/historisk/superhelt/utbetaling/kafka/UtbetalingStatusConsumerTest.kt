package no.nav.historisk.superhelt.utbetaling.kafka

import no.nav.helved.StatusType
import no.nav.helved.UtbetalingStatusMelding
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.utbetaling.*
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.timeout
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional
import java.util.*

@MockedSpringBootTest
//@TestPropertySource(properties = ["app.utbetaling.status-topic=test-status-topic"])
@Transactional
class UtbetalingStatusConsumerTest {


    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, UtbetalingStatusMelding>

    @Autowired
    private lateinit var utbetalingProperties: UtbetalingConfigProperties

    @MockitoBean
    private lateinit var utbetalingRepository: UtbetalingRepository

    @MockitoBean
    private lateinit var utbetalingService: UtbetalingService


    @Test
    fun `should update status to UTBETALT when status is OK`() {
        val utbetaling = sendUtbetaling(StatusType.OK)
        verify(utbetalingService, timeout(1000)).updateUtbetalingsStatus(utbetaling, UtbetalingStatus.UTBETALT)
    }


    @Test
    fun `should update status to FEILET when status is FEILET`() {
        val utbetaling = sendUtbetaling(StatusType.FEILET)
        verify(utbetalingService, timeout(1000)).updateUtbetalingsStatus(utbetaling, UtbetalingStatus.FEILET)
    }

    @Test
    fun `should update status to MOTTATT_AV_UTBETALING when status is MOTTATT`() {
        val utbetaling = sendUtbetaling(StatusType.MOTTATT)
        verify(utbetalingService, timeout(1000)).updateUtbetalingsStatus(
            utbetaling,
            UtbetalingStatus.MOTTATT_AV_UTBETALING
        )
    }

    @Test
    fun `should update status to BEHANDLET_AV_UTBETALING when status is HOS_OPPDRAG`() {
        val utbetaling = sendUtbetaling(StatusType.HOS_OPPDRAG)
        verify(utbetalingService, timeout(1000)).updateUtbetalingsStatus(
            utbetaling,
            UtbetalingStatus.BEHANDLET_AV_UTBETALING
        )
    }

    @Test
    fun `should ignore message if utbetaling not found`() {
        val uuid = UUID.randomUUID()
        whenever(utbetalingRepository.findByTransaksjonsId(uuid)).thenReturn(null)
        val melding = UtbetalingStatusMelding(status = StatusType.OK)

        sendKafkaMessage(uuid, melding, fagsystemHeader = "HISTORISK")
        verify(utbetalingService, never()).updateUtbetalingsStatus(any(), any())
    }

    @Test
    fun `should ignore message with other header than HISTORISK`() {
        val uuid = UUID.randomUUID()
        val melding = UtbetalingStatusMelding(status = StatusType.OK)
        sendKafkaMessage(uuid, melding, fagsystemHeader = "ANNET_FAGSYSTEM")

        verify(utbetalingRepository, never()).findByTransaksjonsId(any())
        verify(utbetalingService, never()).updateUtbetalingsStatus(any(), any())
    }

    @Test
    fun `should ignore message without fagsystem header`() {
        val uuid = UUID.randomUUID()
        val melding = UtbetalingStatusMelding(status = StatusType.OK)
        sendKafkaMessage(uuid, melding, fagsystemHeader = null)

        verify(utbetalingRepository, never()).findByTransaksjonsId(any())
        verify(utbetalingService, never()).updateUtbetalingsStatus(any(), any())
    }


    private fun sendUtbetaling(status: StatusType, fagsystemHeader: String? = "HISTORISK"): Utbetaling {
        val utbetaling = UtbetalingTestData.utbetalingMinimum()
        val uuid = utbetaling.transaksjonsId
        whenever(utbetalingRepository.findByTransaksjonsId(uuid)).thenReturn(utbetaling)
        val melding = UtbetalingStatusMelding(status = status)
        sendKafkaMessage(uuid, melding, fagsystemHeader)
        return utbetaling
    }

    private fun sendKafkaMessage(uuid: UUID, melding: UtbetalingStatusMelding, fagsystemHeader: String?) {
        val record = ProducerRecord(utbetalingProperties.statusTopic, uuid.toString(), melding)
        fagsystemHeader?.let { record.headers().add("fagsystem", it.toByteArray()) }
        kafkaTemplate.send(record)
    }

}
