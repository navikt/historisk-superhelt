package no.nav.historisk.superhelt.utbetaling.kafka

import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.StringKafkaTestConsumer
import no.nav.historisk.superhelt.utbetaling.Utbetaling
import no.nav.historisk.superhelt.utbetaling.UtbetalingTestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import tools.jackson.databind.JsonNode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@MockedSpringBootTest
class UtbetalingKafkaProducerIntegrationTest {

    @Autowired
    private lateinit var producer: UtbetalingKafkaProducer

    @Autowired
    private lateinit var consumer: StringKafkaTestConsumer

    @TestConfiguration(proxyBeanMethods = false)
    class UtbetalingKafkaConsumerTestConfiguration(private val utbetalingConfigProperties: UtbetalingConfigProperties) {

        @Bean
        fun utbetalingkafkaConsumer() = StringKafkaTestConsumer(topic = utbetalingConfigProperties.utbetalingTopic)
    }

    @BeforeEach
    fun setUp() {
        consumer.messages.clear()
    }

    private fun enUtbetaling(
        belop: Int = 1234,
        vedtakstidspunkt: Instant = Instant.parse("2026-03-03T14:29:25.211Z"),
    ): Utbetaling = UtbetalingTestData.utbetalingMinimum(belop = belop)
        .copy(utbetalingTidspunkt = vedtakstidspunkt)

    private fun sendOgMottaMelding(utbetaling: Utbetaling = enUtbetaling()): JsonNode {
        val sak = SakTestData.sakMedStatus(SakStatus.FERDIG_ATTESTERT)
        producer.sendTilUtbetaling(sak, utbetaling)
        return consumer.assertMessageReceived()
    }


    @Test
    fun `feltnavn er camelCase`() {
        val json = sendOgMottaMelding()

        assertThat(json.has("id")).isTrue()
        assertThat(json.has("sakId")).isTrue()
        assertThat(json.has("behandlingId")).isTrue()
        assertThat(json.has("personident")).isTrue()
        assertThat(json.has("stønad")).isTrue()
        assertThat(json.has("vedtakstidspunkt")).isTrue()
        assertThat(json.has("periodetype")).isTrue()
        assertThat(json.has("perioder")).isTrue()
        assertThat(json.has("saksbehandler")).isTrue()
        assertThat(json.has("beslutter")).isTrue()
    }

    @Test
    fun `vedtakstidspunkt serialiseres som ISO-8601 UTC med millisekunder`() {
        val json = sendOgMottaMelding(enUtbetaling(vedtakstidspunkt = Instant.parse("2026-03-03T14:29:25.211Z")))

        assertThat(json["vedtakstidspunkt"].asString()).isEqualTo("2026-03-03T14:29:25.211Z")
    }

    @Test
    fun `periodetype serialiseres som enum-navn`() {
        val json = sendOgMottaMelding()

        assertThat(json["periodetype"].asString()).isEqualTo("EN_GANG")
    }

    @Test
    fun `stønad serialiseres som KlasseKode-navn`() {
        val json = sendOgMottaMelding()

        assertThat(json["stønad"].asString()).isNotBlank()
    }

    @Test
    fun `perioder har korrekte feltnavn og LocalDate-format`() {
        val vedtakstidspunkt = Instant.parse("2026-03-03T14:29:25.211Z")
        val forventetDato = LocalDate.ofInstant(vedtakstidspunkt, ZoneOffset.systemDefault()).toString()
        val json = sendOgMottaMelding(enUtbetaling(vedtakstidspunkt = vedtakstidspunkt))

        val perioder = json["perioder"]
        assertThat(perioder.isArray).isTrue()
        assertThat(perioder.size()).isEqualTo(1)

        val periode = perioder[0]
        assertThat(periode.has("fom")).isTrue()
        assertThat(periode.has("tom")).isTrue()
        assertThat(periode.has("beløp")).isTrue()
        assertThat(periode["fom"].asString()).isEqualTo(forventetDato)
        assertThat(periode["tom"].asString()).isEqualTo(forventetDato)
    }

    @Test
    fun `perioder er tom liste ved annullering`() {
        val json = sendOgMottaMelding(enUtbetaling(belop = 0))

        assertThat(json["perioder"].isArray).isTrue()
        assertThat(json["perioder"].size()).isEqualTo(0)
    }

    @Test
    fun `feltverdier stemmer med input fra sak og utbetaling`() {
        val sak = SakTestData.sakMedStatus(SakStatus.FERDIG_ATTESTERT)
        val utbetaling = enUtbetaling(belop = 5000)
        producer.sendTilUtbetaling(sak, utbetaling)
        val json = consumer.assertMessageReceived()

        assertThat(json["sakId"].asString()).isEqualTo(sak.saksnummer.value)
        assertThat(json["behandlingId"].asString()).isEqualTo(sak.behandlingsnummer.toString())
        assertThat(json["personident"].asString()).isEqualTo(sak.fnr.value)
        assertThat(json["saksbehandler"].asString()).isEqualTo(sak.saksbehandler.navIdent.value)
        assertThat(json["beslutter"].asString()).isEqualTo(sak.attestant!!.navIdent.value)
        assertThat(json["perioder"][0]["beløp"].asInt()).isEqualTo(5000)
    }
}
