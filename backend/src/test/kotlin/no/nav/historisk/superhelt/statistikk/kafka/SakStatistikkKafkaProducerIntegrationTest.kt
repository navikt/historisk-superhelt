package no.nav.historisk.superhelt.statistikk.kafka

import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.StringKafkaTestConsumer
import no.nav.sakstatistikk.BehandlingMetode
import no.nav.sakstatistikk.BehandlingType
import no.nav.sakstatistikk.SaksbehandlingsStatistikk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import tools.jackson.databind.JsonNode
import java.time.Instant
import java.time.LocalDate

@MockedSpringBootTest
class SakStatistikkKafkaProducerIntegrationTest {


    @Autowired
    private lateinit var producer: SakStatistikkKafkaProducer

    @Autowired
    private lateinit var consumer: StringKafkaTestConsumer


    @TestConfiguration(proxyBeanMethods = false)
    class StatistikkKafkaConsumerTestConfiguration(private val statistikkProperties: StatistikkConfigProperties) {

        @Bean
        fun sakStatistikkKafkaConsumer() = StringKafkaTestConsumer(topic = statistikkProperties.saksBehandlingStatistikkTopic)
    }



    private fun enStatistikk(
        behandlingResultat: Enum<*>? = null,
        mottattTid: Instant? = null,
        ferdigBehandletTid: Instant? = null,
        utbetaltTid: LocalDate? = null,
    ) = SaksbehandlingsStatistikk(
        behandlingId = "behandling-123",
        sakId = "sak-456",
        saksnummer = "SAK-001",
        aktorId = "1234567890123",
        endretTid = Instant.parse("2024-06-01T10:00:00Z"),
        sakYtelse = EndringsloggType.OPPRETTET_SAK,
        behandlingType = BehandlingType.SØKNAD,
        behandlingStatus = EndringsloggType.OPPRETTET_SAK,
        behandlingMetode = BehandlingMetode.MANUELL,
        behandlingResultat = behandlingResultat,
        mottattTid = mottattTid,
        ferdigBehandletTid = ferdigBehandletTid,
        utbetaltTid = utbetaltTid,
        opprettetAv = "Z123456",
        saksbehandler = "Z123456",
        ansvarligEnhet = "0300",
        fagsystemNavn = "SUPERHELT",
        fagsystemVersjon = "1.0.0",
    )

    private fun sendOgMottaMelding(statistikk: SaksbehandlingsStatistikk): JsonNode {
        producer.registrerStatistikk(statistikk)

        return consumer.assertMessageReceived()
    }

    @BeforeEach
    fun setUp() {
        consumer.clearMessages()
    }


    @Test
    fun `feltnavn er snake_case`() {
        val json = sendOgMottaMelding(enStatistikk())

        assertThat(json.has("behandling_id")).isTrue()
        assertThat(json.has("sak_id")).isTrue()
        assertThat(json.has("sak_ytelse")).isTrue()
        assertThat(json.has("behandling_type")).isTrue()
        assertThat(json.has("behandling_status")).isTrue()
        assertThat(json.has("endret_tid")).isTrue()
        assertThat(json.has("fagsystem_navn")).isTrue()
        assertThat(json.has("fagsystem_versjon")).isTrue()
    }

    @Test
    fun `Instant serialiseres som ISO-8601`() {
        val json = sendOgMottaMelding(
            enStatistikk(
                mottattTid = Instant.parse("2024-06-01T10:30:00.123Z"),
                ferdigBehandletTid = Instant.parse("2024-06-15T14:00:00Z"),
            )
        )

        assertThat(json["endret_tid"].asString()).isEqualTo("2024-06-01T10:00:00Z")
        assertThat(json["mottatt_tid"].asString()).isEqualTo("2024-06-01T10:30:00.123Z")
        assertThat(json["ferdig_behandlet_tid"].asString()).isEqualTo("2024-06-15T14:00:00Z")
    }

    @Test
    fun `LocalDate serialiseres som yyyy-MM-dd`() {
        val json = sendOgMottaMelding(enStatistikk(utbetaltTid = LocalDate.of(2024, 6, 15)))

        assertThat(json["utbetalt_tid"].asString()).isEqualTo("2024-06-15")
    }

    @Test
    fun `null-felter utelates fra meldingen`() {
        val json = sendOgMottaMelding(enStatistikk())

        assertThat(json.has("behandling_resultat")).isFalse()
        assertThat(json.has("mottatt_tid")).isFalse()
        assertThat(json.has("ferdig_behandlet_tid")).isFalse()
        assertThat(json.has("utbetalt_tid")).isFalse()
    }

    @Test
    fun `enum-felter serialiseres som navn`() {
        val json = sendOgMottaMelding(
            enStatistikk(behandlingResultat = EndringsloggType.FERDIGSTILT_SAK)
        )

        assertThat(json["behandling_type"].asString()).isEqualTo("SØKNAD")
        assertThat(json["behandling_status"].asString()).isEqualTo("OPPRETTET_SAK")
        assertThat(json["behandling_metode"].asString()).isEqualTo("MANUELL")
        assertThat(json["behandling_resultat"].asString()).isEqualTo("FERDIGSTILT_SAK")
    }

    @Test
    fun `feltverdier stemmer med input`() {
        val statistikk = enStatistikk()
        val json = sendOgMottaMelding(statistikk)

        assertThat(json["behandling_id"].asString()).isEqualTo(statistikk.behandlingId)
        assertThat(json["sak_id"].asString()).isEqualTo(statistikk.sakId)
        assertThat(json["saksnummer"].asString()).isEqualTo(statistikk.saksnummer)
        assertThat(json["aktor_id"].asString()).isEqualTo(statistikk.aktorId)
        assertThat(json["saksbehandler"].asString()).isEqualTo(statistikk.saksbehandler)
        assertThat(json["ansvarlig_enhet"].asString()).isEqualTo(statistikk.ansvarligEnhet)
        assertThat(json["fagsystem_navn"].asString()).isEqualTo(statistikk.fagsystemNavn)
        assertThat(json["fagsystem_versjon"].asString()).isEqualTo(statistikk.fagsystemVersjon)
    }
}
