package no.nav.historisk.superhelt.utbetaling

import no.nav.helved.UtbetalingMelding
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.withMockedUser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.KafkaException
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.concurrent.CompletableFuture

@MockedSpringBootTest
@WithMockUser(authorities = ["WRITE"])
class UtbetalingServiceTest {

    @Autowired
    private lateinit var utbetalingService: UtbetalingService

    @Autowired
    private lateinit var utbetalingRepository: UtbetalingRepository

    @Autowired
    private lateinit var sakRepository: SakRepository

    @MockitoBean
    private lateinit var kafkaTemplate: KafkaTemplate<String, UtbetalingMelding>


    private fun lagreSakMedUtbetaling(status: UtbetalingStatus = UtbetalingStatus.UTKAST): Sak {
        val sak = withMockedUser {
            SakTestData.lagreNySak(sakRepository, SakTestData.nySakCompleteUtbetaling())
        }
        val utbetalingUuid = sak.utbetaling!!.uuid
        withMockedUser {
            utbetalingRepository.setUtbetalingStatus(utbetalingUuid, status)
        }
        return sak

    }

    private fun mockKafkaSuccess() {
        val future = CompletableFuture<SendResult<String, UtbetalingMelding>>()
        future.complete(mock())
        whenever(kafkaTemplate.send(any<String>(), any<String>(), any<UtbetalingMelding>()))
            .thenReturn(future)
    }

    private fun mockKafkaFailure(exception: Exception = KafkaException("Kafka connection failed")) {
        val future = CompletableFuture<SendResult<String, UtbetalingMelding>>()
        future.completeExceptionally(exception)
        whenever(kafkaTemplate.send(any<String>(), any<String>(), any<UtbetalingMelding>()))
            .thenReturn(future)
    }


    @Test
    fun `skal sende utbetaling når status er UTKAST`() {
        mockKafkaSuccess()
        val sak = lagreSakMedUtbetaling(status = UtbetalingStatus.UTKAST)
        val utbetalingUuid = sak.utbetaling!!.uuid

        utbetalingService.sendTilUtbetaling(sak)

        val oppdatertUtbetaling = utbetalingRepository.findByUuid(utbetalingUuid)
        assertThat(oppdatertUtbetaling?.utbetalingStatus).isEqualTo(UtbetalingStatus.SENDT_TIL_UTBETALING)
        assertThat(oppdatertUtbetaling?.utbetalingTidspunkt).isNotNull()
        verify(kafkaTemplate).send(any<String>(), eq(utbetalingUuid.toString()), any<UtbetalingMelding>())
    }


    @Test
    fun `skal ignorere når status er SENDT_TIL_UTBETALING`() {
        val sak = lagreSakMedUtbetaling(status = UtbetalingStatus.SENDT_TIL_UTBETALING)
        utbetalingService.sendTilUtbetaling(sak)
        verify(kafkaTemplate, never()).send(any<String>(), any<String>(), any<UtbetalingMelding>())
    }

    @Test
    fun `skal ignorere når status er UTBETALT`() {
        val sak = lagreSakMedUtbetaling(status = UtbetalingStatus.UTBETALT)
        utbetalingService.sendTilUtbetaling(sak)
        verify(kafkaTemplate, never()).send(any<String>(), any<String>(), any<UtbetalingMelding>())
    }


    @Test
    fun `skal ikke gjøre noe når sak mangler utbetaling`() {
        val sak = SakTestData.sakUtenUtbetaling()
        utbetalingService.sendTilUtbetaling(sak)
        verify(kafkaTemplate, never()).send(any<String>(), any<String>(), any<UtbetalingMelding>())
    }

    @Test
    fun `skal endre status på utbetaling selv om kafka feiler`() {
        mockKafkaFailure()
        val sak = lagreSakMedUtbetaling(status = UtbetalingStatus.UTKAST)
        val utbetalingUuid = sak.utbetaling!!.uuid

        assertThrows<Exception> {
            utbetalingService.sendTilUtbetaling(sak)
        }

        val utbetaling = utbetalingRepository.findByUuid(utbetalingUuid)
        assertThat(utbetaling?.utbetalingStatus).isEqualTo(UtbetalingStatus.KLAR_TIL_UTBETALING)
        verify(kafkaTemplate).send(any<String>(), eq(utbetalingUuid.toString()), any<UtbetalingMelding>())
    }


    @Test
    fun `skal sende korrekt melding til kafka topic`() {
        mockKafkaSuccess()

        val sak = lagreSakMedUtbetaling(status = UtbetalingStatus.UTKAST)
        val utbetalingUuid = sak.utbetaling!!.uuid

        utbetalingService.sendTilUtbetaling(sak)

        verify(kafkaTemplate).send(
            any<String>(),
            eq(utbetalingUuid.toString()),
            argThat { melding ->
                melding.id == utbetalingUuid.toString() &&
                        melding.sakId == sak.saksnummer.value &&
                        melding.behandlingId == sak.behandlingsnummer.toString() &&
                        melding.personident == sak.fnr.value &&
                        melding.perioder.first().beløp == sak.utbetaling!!.belop.value &&
                        melding.saksbehandler == sak.saksbehandler.navIdent.value
            }
        )
        val utbetaling = utbetalingRepository.findByUuid(utbetalingUuid)
        assertThat(utbetaling?.utbetalingStatus).isEqualTo(UtbetalingStatus.SENDT_TIL_UTBETALING)
    }

    @Test
    fun `skal sende når status er Klar til utbetaling`() {
        mockKafkaSuccess()
        val sak = lagreSakMedUtbetaling(status = UtbetalingStatus.KLAR_TIL_UTBETALING)
        val utbetalingUuid = sak.utbetaling!!.uuid

        utbetalingService.sendTilUtbetaling(sak)

        verify(kafkaTemplate).send(
            any<String>(),
            eq(utbetalingUuid.toString()),
            any<UtbetalingMelding>()
        )
        val utbetaling = utbetalingRepository.findByUuid(utbetalingUuid)
        assertThat(utbetaling?.utbetalingStatus).isEqualTo(UtbetalingStatus.SENDT_TIL_UTBETALING)
    }

    @Test
    fun `skal håndtere kafka broker unavailable exception`() {
        mockKafkaFailure(KafkaException("Broker not available"))
        val sak = lagreSakMedUtbetaling(status = UtbetalingStatus.UTKAST)

        assertThrows<Exception> {
            utbetalingService.sendTilUtbetaling(sak)
        }

        val utbetaling = utbetalingRepository.findByUuid(sak.utbetaling!!.uuid)
        assertThat(utbetaling?.utbetalingStatus).isEqualTo(UtbetalingStatus.KLAR_TIL_UTBETALING)
    }

    @Test
    fun `skal håndtere serialization exception fra kafka`() {
        mockKafkaFailure(RuntimeException("Serialization failed"))
        val sak = lagreSakMedUtbetaling(status = UtbetalingStatus.UTKAST)

        assertThrows<Exception> {
            utbetalingService.sendTilUtbetaling(sak)
        }

        val utbetaling = utbetalingRepository.findByUuid(sak.utbetaling!!.uuid)
        assertThat(utbetaling?.utbetalingStatus).isEqualTo(UtbetalingStatus.KLAR_TIL_UTBETALING)
    }

}