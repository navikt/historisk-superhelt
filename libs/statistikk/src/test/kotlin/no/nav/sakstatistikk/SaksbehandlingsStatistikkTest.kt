package no.nav.sakstatistikk

import BehandlingMetode
import SaksbehandlingsStatistikk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper
import java.time.Instant
import java.time.LocalDate

class SaksbehandlingsStatistikkTest {

    private lateinit var mapper: JsonMapper

    @BeforeEach
    fun setup() {
        mapper = JsonMapper.builder()
            .findAndAddModules()
            .build()
    }

    private fun enStatistikk(
        endretTid: Instant = Instant.parse("2024-03-06T08:24:24.000Z"),
        tekniskTid: Instant = Instant.parse("2024-03-06T08:24:24.000Z"),
    ) = SaksbehandlingsStatistikk(
        behandlingId = "behandling-1",
        sakId = "sak-1",
        saksnummer = "SAK-001",
        aktorId = "1234567890123",
        endretTid = endretTid,
        tekniskTid = tekniskTid,
        sakYtelse = "ORTOSE",
        behandlingType = "SOKNAD",
        behandlingStatus = "AVSLUTTET",
        behandlingMetode = BehandlingMetode.MANUELL,
        opprettetAv = "Z123456",
        ansvarligEnhet = "0300",
        fagsystemNavn = "historisk-superhelt",
        fagsystemVersjon = "1.0.0",
    )

    @Test
    fun `feltnavn skal være snake_case`() {
        val json = mapper.readTree(mapper.writeValueAsString(enStatistikk()))

        assertThat(json.has("behandling_id")).isTrue()
        assertThat(json.has("sak_id")).isTrue()
        assertThat(json.has("sak_ytelse")).isTrue()
        assertThat(json.has("behandling_type")).isTrue()
        assertThat(json.has("behandling_status")).isTrue()
        assertThat(json.has("behandling_metode")).isTrue()
        assertThat(json.has("opprettet_av")).isTrue()
        assertThat(json.has("ansvarlig_enhet")).isTrue()
        assertThat(json.has("endret_tid")).isTrue()
        assertThat(json.has("teknisk_tid")).isTrue()
        assertThat(json.has("fagsystem_navn")).isTrue()
        assertThat(json.has("fagsystem_versjon")).isTrue()
    }

    @Test
    fun `feltnavn skal ikke være camelCase`() {
        val json = mapper.readTree(mapper.writeValueAsString(enStatistikk()))

        assertThat(json.has("behandlingId")).isFalse()
        assertThat(json.has("sakId")).isFalse()
        assertThat(json.has("sakYtelse")).isFalse()
        assertThat(json.has("behandlingType")).isFalse()
        assertThat(json.has("endretTid")).isFalse()
        assertThat(json.has("tekniskTid")).isFalse()
    }

    @Test
    fun `Instant skal serialiseres som ISO-8601 med tidssone`() {
        val tidspunkt = Instant.parse("2024-03-06T08:24:24.123Z")
        val statistikk = enStatistikk(endretTid = tidspunkt, tekniskTid = tidspunkt)
        val json = mapper.readTree(mapper.writeValueAsString(statistikk))

        val endretTid = json["endret_tid"].asString()
        val tekniskTid = json["teknisk_tid"].asString()

        assertThat(endretTid).isEqualTo("2024-03-06T08:24:24.123Z")
        assertThat(tekniskTid).isEqualTo("2024-03-06T08:24:24.123Z")
    }

    @Test
    fun `valgfri Instant skal serialiseres som ISO-8601 med tidssone`() {
        val tidspunkt = Instant.parse("2024-06-15T12:00:00.00Z")
        val statistikk = enStatistikk().copy(
            mottattTid = tidspunkt,
            registrertTid = tidspunkt,
            ferdigBehandletTid = tidspunkt,
        )
        val json = mapper.readTree(mapper.writeValueAsString(statistikk))

        assertThat(json["mottatt_tid"].asString()).isEqualTo("2024-06-15T12:00:00.000Z")
        assertThat(json["registrert_tid"].asString()).isEqualTo("2024-06-15T12:00:00.000Z")
        assertThat(json["ferdig_behandlet_tid"].asString()).isEqualTo("2024-06-15T12:00:00.000Z")
    }

    @Test
    fun `LocalDate skal serialiseres som yyyy-MM-dd`() {
        val dato = LocalDate.of(2024, 3, 6)
        val statistikk = enStatistikk().copy(
            utbetaltTid = dato,
            funksjonellPeriodeFom = dato,
            funksjonellPeriodeTom = LocalDate.of(2024, 12, 31),
        )
        val json = mapper.readTree(mapper.writeValueAsString(statistikk))

        assertThat(json["utbetalt_tid"].asString()).isEqualTo("2024-03-06")
        assertThat(json["funksjonell_periode_fom"].asString()).isEqualTo("2024-03-06")
        assertThat(json["funksjonell_periode_tom"].asString()).isEqualTo("2024-12-31")
    }

    @Test
    fun `null-felter skal ikke inkluderes i JSON`() {
        val json = mapper.readTree(mapper.writeValueAsString(enStatistikk()))

        assertThat(json.has("mottatt_tid")).isFalse()
        assertThat(json.has("registrert_tid")).isFalse()
        assertThat(json.has("ferdig_behandlet_tid")).isFalse()
        assertThat(json.has("utbetalt_tid")).isFalse()
        assertThat(json.has("behandling_resultat")).isFalse()
        assertThat(json.has("resultat_begrunnelse")).isFalse()
        assertThat(json.has("sak_utland")).isFalse()
    }

    @Test
    fun `JSON skal kunne deserialiseres tilbake til dataklasse`() {
        val original = enStatistikk().copy(
            mottattTid = Instant.parse("2024-03-06T08:00:00.00Z"),
            utbetaltTid = LocalDate.of(2024, 3, 6),
            behandlingResultat = "INNVILGET",
        )
        val json = mapper.writeValueAsString(original)
        val deserialisert = mapper.readValue(json, SaksbehandlingsStatistikk::class.java)

        assertThat(deserialisert).isEqualTo(original)
    }
}
