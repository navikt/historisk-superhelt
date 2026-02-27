package no.nav.historisk.superhelt.sak

import no.nav.common.types.Belop
import no.nav.historisk.superhelt.utbetaling.UtbetalingTestData
import no.nav.historisk.superhelt.utbetaling.UtbetalingsType
import no.nav.historisk.superhelt.vedtak.VedtaksResultat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SakValidatorFieldsTest {

    fun okSak() = SakTestData.sakMedUtbetaling().copy(
        vedtaksResultat = VedtaksResultat.INNVILGET
    )

    @Nested
    inner class TittelValidation {

        @Test
        fun `should pass when tittel is set and within length limit`() {
            val sak = okSak()
            val validator = SakValidator(sak)

            validator.checkSoknad()

            assertThat(validator.validationErrors).isEmpty()
        }

        @Test
        fun `should fail when beskrivelse is null or blank`() {
            val sak = okSak().copy(beskrivelse = null)
            val validator = SakValidator(sak)

            validator.checkSoknad()

            assertThat(validator.validationErrors)
                .hasSize(1)
                .anyMatch { it.field == "beskrivelse" && it.message.contains("Beskrivelse må være satt") }
        }

        @Test
        fun `should fail when beskrivelse exceeds 200 characters`() {
            val sak = okSak().copy(beskrivelse = "a".repeat(201))
            val validator = SakValidator(sak)

            validator.checkSoknad()

            assertThat(validator.validationErrors)
                .hasSize(1)
                .anyMatch { it.field == "beskrivelse" && it.message.contains("Beskrivelse kan ikke være lengre enn 200 tegn") }
        }
    }

    @Nested
    inner class SoknadsDatoValidation {

        @Test
        fun `should pass when soknadsDato is set`() {
            val sak = okSak().copy(soknadsDato = LocalDate.now())
            val validator = SakValidator(sak)

            validator.checkSoknad()

            assertThat(validator.validationErrors).isEmpty()
        }

        @Test
        fun `should fail when soknadsDato is null`() {
            val sak = okSak().copy(soknadsDato = null)
            val validator = SakValidator(sak)

            validator.checkSoknad()

            assertThat(validator.validationErrors)
                .hasSize(1)
                .anyMatch { it.field == "soknadsDato" && it.message.contains("Søknadsdato må være satt") }
        }
    }

    @Nested
    inner class BegrunnelseValidation {

        @Test
        fun `should pass when begrunnelse is null`() {
            val sak = okSak().copy(begrunnelse = null)
            val validator = SakValidator(sak)

            validator.checkSoknad()

            assertThat(validator.validationErrors).isEmpty()
        }

        @Test
        fun `should pass when begrunnelse is within length limit`() {
            val sak = okSak().copy(begrunnelse = "Valid begrunnelse")
            val validator = SakValidator(sak)

            validator.checkSoknad()

            assertThat(validator.validationErrors).isEmpty()
        }

        @Test
        fun `should fail when begrunnelse exceeds 1000 characters`() {
            val sak = okSak().copy(begrunnelse = "a".repeat(1001))
            val validator = SakValidator(sak)

            validator.checkSoknad()

            assertThat(validator.validationErrors)
                .hasSize(1)
                .anyMatch { it.field == "begrunnelse" && it.message.contains("Begrunnelse kan ikke være lengre enn 1000 tegn") }
        }
    }

    @Nested
    inner class VedtaksResultatValidation {

        @Test
        fun `should pass when vedtaksResultat is set`() {
            val sak = okSak().copy(vedtaksResultat = VedtaksResultat.AVSLATT)
            val validator = SakValidator(sak)

            validator.checkSoknad()

            assertThat(validator.validationErrors).isEmpty()
        }

        @Test
        fun `should fail when vedtaksResultat is null`() {
            val sak = okSak().copy(vedtaksResultat = null)
            val validator = SakValidator(sak)

            validator.checkSoknad()

            assertThat(validator.validationErrors)
                .hasSize(1)
                .anyMatch { it.field == "vedtaksResultat" && it.message.contains("Vedtak må være satt") }
        }
    }

    @Nested
    inner class UtbetalingForhandstilsagnValidation {

        @Test
        fun `should pass when vedtaksResultat is INNVILGET and utbetaling is set`() {
            val sak = okSak().copy(vedtaksResultat = VedtaksResultat.INNVILGET)
            val validator = SakValidator(sak)

            validator.checkSoknad()

            assertThat(validator.validationErrors).isEmpty()
        }

        @Test
        fun `should pass when vedtaksResultat is DELVIS_INNVILGET and forhandstilsagn is set`() {
            val sak = okSak().copy(
                vedtaksResultat = VedtaksResultat.DELVIS_INNVILGET,
                utbetalingsType = UtbetalingsType.FORHANDSTILSAGN,
            )
            val validator = SakValidator(sak)

            validator.checkSoknad()

            assertThat(validator.validationErrors).isEmpty()
        }

        @Test
        fun `should fail when vedtaksResultat is INNVILGET and neither utbetaling nor forhandstilsagn is set`() {
            val sak = okSak().copy(
                vedtaksResultat = VedtaksResultat.INNVILGET,
                utbetalingsType = UtbetalingsType.INGEN,
                belop = null,
            )
            val validator = SakValidator(sak)

            validator.checkSoknad()

            assertThat(validator.validationErrors)
                .hasSize(1)
                .anyMatch { it.field == "utbetaling" && it.message.contains("Det må velges enten utbetaling eller forhandstilsagn") }
        }

        @Test
        fun `should fail when vedtaksResultat is DELVIS_INNVILGET and neither utbetaling nor forhandstilsagn is set`() {
            val sak = okSak().copy(
                vedtaksResultat = VedtaksResultat.DELVIS_INNVILGET,
                utbetalingsType = UtbetalingsType.INGEN,
                belop = null,
            )
            val validator = SakValidator(sak)

            validator.checkSoknad()

            assertThat(validator.validationErrors)
                .hasSize(1)
                .anyMatch { it.field == "utbetaling" && it.message.contains("Det må velges enten utbetaling eller forhandstilsagn") }
        }
    }

    @Nested
    inner class UtbetalingsTypeValidation {

        @Test
        fun `should pass when utbetalingsType is BRUKER and utbetaling has positive belop`() {
            val sak = okSak().copy(
                vedtaksResultat = VedtaksResultat.INNVILGET
            )
            val validator = SakValidator(sak)

            validator.checkSoknad()

            assertThat(validator.validationErrors).isEmpty()
        }

        @Test
        fun `should fail when utbetalingsType is BRUKER and utbetaling belop is zero or negative`() {
            val sak = okSak().copy(
                vedtaksResultat = VedtaksResultat.INNVILGET,
                utbetalingsType = UtbetalingsType.BRUKER,
                belop = Belop(0),
            )
            val validator = SakValidator(sak)

            validator.checkSoknad()

            assertThat(validator.validationErrors)
                .hasSize(1)
                .anyMatch { it.field == "utbetaling.belop" && it.message.contains("Beløpet må settes og være positivt") }
        }

        @Test
        fun `should pass when utbetalingsType is FORHANDSTILSAGN`() {
            val sak = okSak().copy(
                vedtaksResultat = VedtaksResultat.INNVILGET,
                utbetalingsType = UtbetalingsType.FORHANDSTILSAGN,
                belop = null,
            )
            val validator = SakValidator(sak)

            validator.checkSoknad()

            assertThat(validator.validationErrors).isEmpty()
        }

    }

}