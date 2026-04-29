package no.nav.historisk.superhelt.utbetaling.kafka

import no.nav.helved.UtbetalingMelding
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.utbetaling.UtbetalingRepository
import no.nav.historisk.superhelt.utbetaling.UtbetalingTestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

@ExtendWith(MockitoExtension::class)
class UtbetalingKafkaProducerTest {

    @Mock
    private lateinit var kafkaTemplate: KafkaTemplate<String, UtbetalingMelding>

    @Mock
    private lateinit var utbetalingRepository: UtbetalingRepository

    private val topic = "test-utbetaling-topic"
    private lateinit var producer: UtbetalingKafkaProducer

    @BeforeEach
    fun setUp() {
        producer = UtbetalingKafkaProducer(
            kafkaTemplate = kafkaTemplate,
            utbetalingRepository = utbetalingRepository,
            properties = UtbetalingConfigProperties(utbetalingTopic = topic, statusTopic = "test-status-topic")
        )
    }

    private fun mockKafkaSuccess() {
        val future = CompletableFuture<SendResult<String, UtbetalingMelding>>()
        future.complete(mock())
        whenever(kafkaTemplate.send(any<String>(), any<String>(), any<UtbetalingMelding>()))
            .thenReturn(future)
    }

    private fun mockKafkaFailure(cause: Exception = RuntimeException("Kafka feilet")) {
        val future = CompletableFuture<SendResult<String, UtbetalingMelding>>()
        future.completeExceptionally(cause)
        whenever(kafkaTemplate.send(any<String>(), any<String>(), any<UtbetalingMelding>()))
            .thenReturn(future)
    }

    @Nested
    inner class SendTilUtbetaling {

        @Nested
        inner class Melding {

            @Test
            fun `skal sende til korrekt topic med transaksjonsId som key`() {
                mockKafkaSuccess()
                val sak = SakTestData.sakMedUtbetaling()
                val utbetaling = UtbetalingTestData.utbetalingMinimum()

                producer.sendTilUtbetaling(sak, utbetaling)

                verify(kafkaTemplate).send(eq(topic), eq(utbetaling.transaksjonsId.toString()), any())
            }

            @Test
            fun `skal sende melding med korrekte felter fra sak og utbetaling`() {
                mockKafkaSuccess()
                val sak = SakTestData.sakMedUtbetaling()
                val utbetaling = UtbetalingTestData.utbetalingMinimum().copy(klasseKode = sak.type.defaultKlasseKode )

                producer.sendTilUtbetaling(sak, utbetaling)

                verify(kafkaTemplate).send(any(), any(), argThat { melding ->
                    melding.id == utbetaling.utbetalingsUuid &&
                        melding.sakId == sak.saksnummer.value &&
                        melding.behandlingId == sak.behandlingsnummer.toString() &&
                        melding.personident == sak.fnr.value &&
                        melding.stønad == sak.type.defaultKlasseKode &&
                        melding.saksbehandler == sak.saksbehandler.navIdent.value
                })
            }

            @Test
            fun `skal bruke attestant som beslutter når attestant er satt`() {
                mockKafkaSuccess()
                val sak = SakTestData.sakMedStatus(SakStatus.FERDIG_ATTESTERT)
                val utbetaling = UtbetalingTestData.utbetalingMinimum()

                producer.sendTilUtbetaling(sak, utbetaling)

                verify(kafkaTemplate).send(any(), any(), argThat { melding ->
                    melding.beslutter == sak.attestant!!.navIdent.value
                })
            }

            @Test
            fun `skal bruke saksbehandler som beslutter når attestant mangler`() {
                mockKafkaSuccess()
                val sak = SakTestData.sakMedUtbetaling() // attestant = null
                val utbetaling = UtbetalingTestData.utbetalingMinimum()

                producer.sendTilUtbetaling(sak, utbetaling)

                verify(kafkaTemplate).send(any(), any(), argThat { melding ->
                    melding.beslutter == sak.saksbehandler.navIdent.value
                })
            }

            @Test
            fun `skal bruke utbetalingTidspunkt fra utbetaling som vedtakstidspunkt`() {
                mockKafkaSuccess()
                val sak = SakTestData.sakMedUtbetaling()
                val utbetaling = UtbetalingTestData.utbetalingMinimum().copy(utbetalingTidspunkt = Instant.now())
                assertThat(utbetaling.utbetalingTidspunkt).isNotNull()

                producer.sendTilUtbetaling(sak, utbetaling)

                verify(kafkaTemplate).send(any(), any(), argThat { melding ->
                    melding.vedtakstidspunkt == utbetaling.utbetalingTidspunkt
                })
            }
        }

        @Nested
        inner class Annullering {

            @Test
            fun `skal sende med tom periode-liste ved annullering (belop er 0)`() {
                mockKafkaSuccess()
                val sak = SakTestData.sakMedUtbetaling()
                val annullering = UtbetalingTestData.utbetalingMinimum(belop = 0)
                assertThat(annullering.annulleres).isTrue()

                producer.sendTilUtbetaling(sak, annullering)

                verify(kafkaTemplate).send(any(), any(), argThat { melding ->
                    melding.perioder.isEmpty()
                })
            }

            @Test
            fun `skal sende med periode og beløp ved normal utbetaling`() {
                mockKafkaSuccess()
                val sak = SakTestData.sakMedUtbetaling()
                val utbetaling = UtbetalingTestData.utbetalingMinimum(belop = 5000)
                assertThat(utbetaling.annulleres).isFalse()

                producer.sendTilUtbetaling(sak, utbetaling)

                verify(kafkaTemplate).send(any(), any(), argThat { melding ->
                    melding.perioder.size == 1 &&
                        melding.perioder.first().beløp == 5000
                })
            }

        }

        @Nested
        inner class StatusOppdatering {

            @Test
            fun `skal sette status SENDT etter vellykket kafka-sending`() {
                mockKafkaSuccess()
                val sak = SakTestData.sakMedUtbetaling()
                val utbetaling = UtbetalingTestData.utbetalingMinimum()

                producer.sendTilUtbetaling(sak, utbetaling)

                verify(utbetalingRepository).setUtbetalingStatusSendt(utbetaling.transaksjonsId)
            }

            @Test
            fun `skal ikke sette status SENDT når kafka feiler`() {
                mockKafkaFailure()
                val sak = SakTestData.sakMedUtbetaling()
                val utbetaling = UtbetalingTestData.utbetalingMinimum()

                assertThrows<ExecutionException> {
                    producer.sendTilUtbetaling(sak, utbetaling)
                }

                verify(utbetalingRepository, never()).setUtbetalingStatusSendt(any())
            }

            @Test
            fun `skal kaste exception videre når kafka feiler`() {
                mockKafkaFailure(RuntimeException("Broker utilgjengelig"))
                val sak = SakTestData.sakMedUtbetaling()
                val utbetaling = UtbetalingTestData.utbetalingMinimum()

                assertThrows<ExecutionException> {
                    producer.sendTilUtbetaling(sak, utbetaling)
                }
            }
        }
    }
}
