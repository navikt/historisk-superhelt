package no.nav.historisk.superhelt.statistikk

import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.statistikk.kafka.SakStatistikkKafkaProducer
import no.nav.historisk.superhelt.statistikk.kafka.StatistikkConfigProperties
import no.nav.sakstatistikk.SaksbehandlingsStatistikk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import java.time.Instant
import java.util.concurrent.CompletableFuture

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StatistikkServiceTest {

    @Mock
    private lateinit var kafkaTemplate: KafkaTemplate<String, SaksbehandlingsStatistikk>

    private lateinit var service: StatistikkService
    private lateinit var mapper: JsonMapper

    @BeforeEach
    fun setUp() {
        val producer = SakStatistikkKafkaProducer(
            kafkaTemplate = kafkaTemplate,
            properties = StatistikkConfigProperties(saksBehandlingStatistikkTopic = "test-statistikk-topic")
        )
        service = StatistikkService(producer, "1.0.0-test")
        mapper = JsonMapper.builder().findAndAddModules().build()

        val future = CompletableFuture<SendResult<String, SaksbehandlingsStatistikk>>()
        future.complete(mock())
        whenever(kafkaTemplate.send(any<String>(), any<String>(), any<SaksbehandlingsStatistikk>()))
            .thenReturn(future)
    }

    private fun fangetJson(): JsonNode {
        val captor = argumentCaptor<SaksbehandlingsStatistikk>()
        verify(kafkaTemplate).send(any(), any(), captor.capture())
        return mapper.readTree(mapper.writeValueAsString(captor.firstValue))
    }

    @Nested
    inner class JsonFormat {

        @Test
        fun `feltnavn skal være snake_case`() {
            val sak = SakTestData.sakUtenUtbetaling()
            service.handleEvent(EndringsloggType.OPPRETTET_SAK, Instant.now(), sak)

            val json = fangetJson()

            assertThat(json.has("behandling_id")).isTrue()
            assertThat(json.has("sak_ytelse")).isTrue()
            assertThat(json.has("behandling_status")).isTrue()
            assertThat(json.has("behandling_type")).isTrue()
            assertThat(json.has("endret_tid")).isTrue()
            assertThat(json.has("registrert_tid")).isTrue()
            assertThat(json.has("fagsystem_navn")).isTrue()
            assertThat(json.has("fagsystem_versjon")).isTrue()
        }

        @Test
        fun `feltnavn skal ikke være camelCase`() {
            val sak = SakTestData.sakUtenUtbetaling()
            service.handleEvent(EndringsloggType.OPPRETTET_SAK, Instant.now(), sak)

            val json = fangetJson()

            assertThat(json.has("behandlingId")).isFalse()
            assertThat(json.has("sakYtelse")).isFalse()
            assertThat(json.has("behandlingStatus")).isFalse()
            assertThat(json.has("endretTid")).isFalse()
        }

        @Test
        fun `Instant-felter skal serialiseres som ISO-8601 med tidssone`() {
            val tidspunkt = Instant.parse("2024-06-01T10:30:00.123Z")
            service.handleEvent(EndringsloggType.FERDIGSTILT_SAK, tidspunkt, SakTestData.sakMedStatus(SakStatus.FERDIG))

            val json = fangetJson()

            assertThat(json["ferdig_behandlet_tid"].asString()).isEqualTo("2024-06-01T10:30:00.123Z")
        }

        @Test
        fun `null-felter skal ikke inkluderes i JSON`() {
            val sak = SakTestData.sakUtenUtbetaling() // ingen attestant, ingen mottattTid etc.
            service.handleEvent(EndringsloggType.OPPRETTET_SAK, Instant.now(), sak)

            val json = fangetJson()

            assertThat(json.has("behandling_resultat")).isFalse()
            assertThat(json.has("resultat_begrunnelse")).isFalse()
            assertThat(json.has("mottatt_tid")).isFalse()
            assertThat(json.has("ferdig_behandlet_tid")).isFalse()
            assertThat(json.has("ansvarlig_beslutter")).isFalse()
        }

        @Test
        fun `enum-felter skal serialiseres som navn-streng`() {
            val sak = SakTestData.sakUtenUtbetaling()
            service.handleEvent(EndringsloggType.TIL_ATTESTERING, Instant.now(), sak)

            val json = fangetJson()

            assertThat(json["behandling_status"].asString()).isEqualTo("TIL_ATTESTERING")
            assertThat(json["behandling_type"].asString()).isEqualTo("SØKNAD")
            assertThat(json["behandling_resultat"].asString()).isEqualTo(sak.vedtaksResultat!!.name)
        }
    }

    @Nested
    inner class IngenMeldingSendt {

        @Test
        fun `SENDT_BREV skal ikke sende statistikk`() {
            service.handleEvent(EndringsloggType.SENDT_BREV, Instant.now(), SakTestData.sakUtenUtbetaling())

            verify(kafkaTemplate, never()).send(any<String>(), any<String>(), any())
        }

        @Test
        fun `UTBETALING_OK skal ikke sende statistikk`() {
            service.handleEvent(EndringsloggType.UTBETALING_OK, Instant.now(), SakTestData.sakUtenUtbetaling())

            verify(kafkaTemplate, never()).send(any<String>(), any<String>(), any())
        }

        @Test
        fun `UTBETALING_FEILET skal ikke sende statistikk`() {
            service.handleEvent(EndringsloggType.UTBETALING_FEILET, Instant.now(), SakTestData.sakUtenUtbetaling())

            verify(kafkaTemplate, never()).send(any<String>(), any<String>(), any())
        }
    }
}
