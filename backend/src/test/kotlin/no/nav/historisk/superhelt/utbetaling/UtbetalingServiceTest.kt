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


    private fun lagreSakMedUtbetaling(status: UtbetalingStatus = UtbetalingStatus.UTKAST): Pair<Sak, Utbetaling> {
        val savedSak = withMockedUser {
            SakTestData.lagreNySak(sakRepository, SakTestData.nySakCompleteUtbetaling())
        }
        // Opprett utbetaling-rad i DB (simulerer ferdigstilling)
        val utbetaling = utbetalingRepository.opprettUtbetaling(savedSak)
        withMockedUser {
            utbetalingRepository.setUtbetalingStatus(utbetaling.uuid, status)
        }
        return Pair(savedSak, utbetalingRepository.findByUuid(utbetaling.uuid)!!)
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
        val (sak, utbetaling) = lagreSakMedUtbetaling(status = UtbetalingStatus.UTKAST)

        utbetalingService.sendTilUtbetaling(sak)

        val oppdatertUtbetaling = utbetalingRepository.findByUuid(utbetaling.uuid)
        assertThat(oppdatertUtbetaling?.utbetalingStatus).isEqualTo(UtbetalingStatus.SENDT_TIL_UTBETALING)
        assertThat(oppdatertUtbetaling?.utbetalingTidspunkt).isNotNull()
        verify(kafkaTemplate).send(any<String>(), eq(utbetaling.uuid.toString()), any<UtbetalingMelding>())
    }


    @Test
    fun `skal ignorere når status er SENDT_TIL_UTBETALING`() {
        val (sak, _) = lagreSakMedUtbetaling(status = UtbetalingStatus.SENDT_TIL_UTBETALING)
        utbetalingService.sendTilUtbetaling(sak)
        verify(kafkaTemplate, never()).send(any<String>(), any<String>(), any<UtbetalingMelding>())
    }

    @Test
    fun `skal ignorere når status er UTBETALT`() {
        val (sak, _) = lagreSakMedUtbetaling(status = UtbetalingStatus.UTBETALT)
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
        val (sak, utbetaling) = lagreSakMedUtbetaling(status = UtbetalingStatus.UTKAST)

        assertThrows<Exception> {
            utbetalingService.sendTilUtbetaling(sak)
        }

        val oppdatertUtbetaling = utbetalingRepository.findByUuid(utbetaling.uuid)
        assertThat(oppdatertUtbetaling?.utbetalingStatus).isEqualTo(UtbetalingStatus.KLAR_TIL_UTBETALING)
        verify(kafkaTemplate).send(any<String>(), eq(utbetaling.uuid.toString()), any<UtbetalingMelding>())
    }


    @Test
    fun `skal sende korrekt melding til kafka topic`() {
        mockKafkaSuccess()

        val (sak, utbetaling) = lagreSakMedUtbetaling(status = UtbetalingStatus.UTKAST)

        utbetalingService.sendTilUtbetaling(sak)

        verify(kafkaTemplate).send(
            any<String>(),
            eq(utbetaling.uuid.toString()),
            argThat { melding ->
                melding.id == utbetaling.saksnummer.value &&
                        melding.sakId == sak.saksnummer.value &&
                        melding.behandlingId == sak.behandlingsnummer.toString() &&
                        melding.personident == sak.fnr.value &&
                        melding.perioder.first().beløp == sak.belop!!.value &&
                        melding.saksbehandler == sak.saksbehandler.navIdent.value
            }
        )
        val oppdatertUtbetaling = utbetalingRepository.findByUuid(utbetaling.uuid)
        assertThat(oppdatertUtbetaling?.utbetalingStatus).isEqualTo(UtbetalingStatus.SENDT_TIL_UTBETALING)
    }

    @Test
    fun `skal sende når status er Klar til utbetaling`() {
        mockKafkaSuccess()
        val (sak, utbetaling) = lagreSakMedUtbetaling(status = UtbetalingStatus.KLAR_TIL_UTBETALING)

        utbetalingService.sendTilUtbetaling(sak)

        verify(kafkaTemplate).send(
            any<String>(),
            eq(utbetaling.uuid.toString()),
            any<UtbetalingMelding>()
        )
        val oppdatertUtbetaling = utbetalingRepository.findByUuid(utbetaling.uuid)
        assertThat(oppdatertUtbetaling?.utbetalingStatus).isEqualTo(UtbetalingStatus.SENDT_TIL_UTBETALING)
    }

    @Test
    fun `skal håndtere kafka broker unavailable exception`() {
        mockKafkaFailure(KafkaException("Broker not available"))
        val (sak, utbetaling) = lagreSakMedUtbetaling(status = UtbetalingStatus.UTKAST)

        assertThrows<Exception> {
            utbetalingService.sendTilUtbetaling(sak)
        }

        val oppdatertUtbetaling = utbetalingRepository.findByUuid(utbetaling.uuid)
        assertThat(oppdatertUtbetaling?.utbetalingStatus).isEqualTo(UtbetalingStatus.KLAR_TIL_UTBETALING)
    }

    @Test
    fun `skal opprette ny utbetaling når sak er gjenåpnet med nytt behandlingsnummer`() {
        mockKafkaSuccess()
        val (sak, gammelUtbetaling) = lagreSakMedUtbetaling(status = UtbetalingStatus.UTBETALT)

        val gjenapnetSak = withMockedUser { sakRepository.incrementBehandlingsNummer(sak.saksnummer) }

        utbetalingService.sendTilUtbetaling(gjenapnetSak)

        // Gammel utbetaling skal være uendret
        val gammelOppdatert = utbetalingRepository.findByUuid(gammelUtbetaling.uuid)
        assertThat(gammelOppdatert?.utbetalingStatus).isEqualTo(UtbetalingStatus.UTBETALT)

        // Ny utbetaling skal ha nytt behandlingsnummer
        val nyUtbetaling = utbetalingRepository.findActiveByBehandling(gjenapnetSak)
        assertThat(nyUtbetaling).isNotNull
        assertThat(nyUtbetaling!!.behandlingsnummer).isEqualTo(gjenapnetSak.behandlingsnummer)
        assertThat(nyUtbetaling.uuid).isNotEqualTo(gammelUtbetaling.uuid)
        assertThat(nyUtbetaling.utbetalingStatus).isEqualTo(UtbetalingStatus.SENDT_TIL_UTBETALING)
    }

    @Test
    fun `skal ikke sende på nytt for gammelt behandlingsnummer når ny finnes`() {
        mockKafkaSuccess()
        val sak = withMockedUser { SakTestData.lagreNySak(sakRepository, SakTestData.nySakCompleteUtbetaling()) }

        utbetalingService.sendTilUtbetaling(sak)
        clearInvocations(kafkaTemplate)

        // Samme behandlingsnummer – skal ikke sende på nytt (status er SENDT)
        utbetalingService.sendTilUtbetaling(sak)

        verify(kafkaTemplate, never()).send(any<String>(), any<String>(), any<UtbetalingMelding>())
    }

    @Test
    fun `skal håndtere serialization exception fra kafka`() {
        mockKafkaFailure(RuntimeException("Serialization failed"))
        val (sak, utbetaling) = lagreSakMedUtbetaling(status = UtbetalingStatus.UTKAST)

        assertThrows<Exception> {
            utbetalingService.sendTilUtbetaling(sak)
        }

        val oppdatertUtbetaling = utbetalingRepository.findByUuid(utbetaling.uuid)
        assertThat(oppdatertUtbetaling?.utbetalingStatus).isEqualTo(UtbetalingStatus.KLAR_TIL_UTBETALING)
    }

}