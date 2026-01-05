package no.nav.historisk.superhelt.utbetaling.kafka

import no.nav.helved.StatusType
import no.nav.helved.UtbetalingStatusMelding
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.utbetaling.UtbetalingRepository
import no.nav.historisk.superhelt.utbetaling.UtbetalingService
import no.nav.historisk.superhelt.utbetaling.UtbetalingStatus
import no.nav.historisk.superhelt.utbetaling.UtbetalingTestData
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.timeout
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional
import java.util.*

@MockedSpringBootTest
@TestPropertySource(properties = ["app.utbetaling.status-topic=test-status-topic"])
@Transactional
class UtbetalingStatusConsumerTest {


    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, UtbetalingStatusMelding>

    @MockitoBean
    private lateinit var utbetalingRepository: UtbetalingRepository

    @MockitoBean
    private lateinit var utbetalingService: UtbetalingService


    @Test
    fun `should update status to UTBETALT when status is OK`() {
        val utbetaling = UtbetalingTestData.utbetalingMinimum()
        val uuid = utbetaling.uuid
        `when`(utbetalingRepository.findByUuid(uuid)).thenReturn(utbetaling)

        val melding = UtbetalingStatusMelding(status = StatusType.OK)

        kafkaTemplate.send("test-status-topic", uuid.toString(), melding)


        verify(utbetalingService, timeout(1000)).updateUtbetalingsStatus(utbetaling, UtbetalingStatus.UTBETALT)
    }

    @Test
    fun `should update status to FEILET when status is FEILET`() {
        val utbetaling = UtbetalingTestData.utbetalingMinimum()
        val uuid = utbetaling.uuid
        `when`(utbetalingRepository.findByUuid(uuid)).thenReturn(utbetaling)

        val melding = UtbetalingStatusMelding(status = StatusType.FEILET)

        kafkaTemplate.send("test-status-topic", uuid.toString(), melding)

        verify(utbetalingService, timeout(1000)).updateUtbetalingsStatus(utbetaling, UtbetalingStatus.FEILET)
    }

    @Test
    fun `should update status to MOTTATT_AV_UTBETALING when status is MOTTATT`() {
        val utbetaling = UtbetalingTestData.utbetalingMinimum()
        val uuid = utbetaling.uuid
        `when`(utbetalingRepository.findByUuid(uuid)).thenReturn(utbetaling)

        val melding = UtbetalingStatusMelding(status = StatusType.MOTTATT)

        kafkaTemplate.send("test-status-topic", uuid.toString(), melding)

        verify(utbetalingService, timeout(1000)).updateUtbetalingsStatus(utbetaling, UtbetalingStatus.MOTTATT_AV_UTBETALING)
    }

    @Test
    fun `should update status to BEHANDLET_AV_UTBETALING when status is HOS_OPPDRAG`() {
        val utbetaling = UtbetalingTestData.utbetalingMinimum()
        val uuid = utbetaling.uuid
        `when`(utbetalingRepository.findByUuid(uuid)).thenReturn(utbetaling)

        val melding = UtbetalingStatusMelding(status = StatusType.HOS_OPPDRAG)

        kafkaTemplate.send("test-status-topic", uuid.toString(), melding)

        verify(utbetalingService, timeout(1000)).updateUtbetalingsStatus(utbetaling, UtbetalingStatus.BEHANDLET_AV_UTBETALING)
    }

    @Test
    fun `should ignore message if utbetaling not found`() {
        val uuid = UUID.randomUUID()
        `when`(utbetalingRepository.findByUuid(uuid)).thenReturn(null)

        val melding = UtbetalingStatusMelding(status = StatusType.OK)

        kafkaTemplate.send("test-status-topic", uuid.toString(), melding)

        verify(utbetalingService, never()).updateUtbetalingsStatus(any(), any())
    }
}
