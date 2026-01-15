package no.nav.common.types

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class SaksnummerTest {
    @Test
    fun `should create Saksnummer from Long id`() {
        val saksnummer = Saksnummer(123)
        assertThat(saksnummer.value).isEqualTo("SH-000123")
        assertThat(saksnummer.id).isEqualTo(123)
    }

    @Test
    fun `should create Saksnummer from large number`() {
        val saksnummer = Saksnummer(123456789)
        assertThat(saksnummer.value).isEqualTo("SH-123456789")
        assertThat(saksnummer.id).isEqualTo(123456789)
    }

    @Test
    fun `should create Saksnummer from String value`() {
        val saksnummer = Saksnummer("SH-001234")
        assertThat(saksnummer.value).isEqualTo("SH-001234")
        assertThat(saksnummer.id).isEqualTo(1234)
    }

    @Test
    fun `should give value on toString`() {
        val saksnummer = Saksnummer("SH-000456")
        assertThat(saksnummer.toString()).isEqualTo("SH-000456")
    }

    @Test
    fun `should parse saksnummer with different prefix`() {
        val saksnummer = Saksnummer("AB-000789")
        assertThat(saksnummer.value).isEqualTo("AB-000789")
        assertThat(saksnummer.id).isEqualTo(789)
    }

    @Test
    fun `should throw exception for invalid Saksnummer string`() {
        assertThatThrownBy { Saksnummer("INVALID").id }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("Ugyldig saksnummer: INVALID")
    }
}