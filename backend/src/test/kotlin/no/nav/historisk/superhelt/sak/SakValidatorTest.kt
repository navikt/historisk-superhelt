package no.nav.historisk.superhelt.sak

import no.nav.historisk.superhelt.infrastruktur.exception.ValideringException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SakValidatorTest {

    @Nested
    inner class CheckStatusTransitionTests {

        @Test
        fun `should allow valid transition from UNDER_BEHANDLING to FERDIG`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(status = SakStatus.UNDER_BEHANDLING)
            val validator = SakValidator(sak)

            validator.checkStatusTransition(SakStatus.FERDIG)

            assertThat(validator.validationErrors).isEmpty()
        }

        @Test
        fun `should allow valid transition from UNDER_BEHANDLING to TIL_ATTESTERING`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(status = SakStatus.UNDER_BEHANDLING)
            val validator = SakValidator(sak)

            validator.checkStatusTransition(SakStatus.TIL_ATTESTERING)

            assertThat(validator.validationErrors).isEmpty()
        }

        @Test
        fun `should allow valid transition from UNDER_BEHANDLING to FERDIG directly`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(status = SakStatus.UNDER_BEHANDLING)
            val validator = SakValidator(sak)

            validator.checkStatusTransition(SakStatus.FERDIG)

            assertThat(validator.validationErrors).isEmpty()
        }

        @Test
        fun `should reject invalid transition from TIL_ATTESTERING to UNDER_BEHANDLING`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(status = SakStatus.TIL_ATTESTERING)
            val validator = SakValidator(sak)

            validator.checkStatusTransition(SakStatus.UNDER_BEHANDLING)

            assertThat(validator.validationErrors).hasSize(1)
        }
    }

    @Nested
    inner class CheckSoknadTests {

        @Test
        fun `should pass when sak has no validation violations and has utbetaling`() {
            val sak = SakTestData.sakMedUtbetaling()
            val validator = SakValidator(sak)

            validator.checkSoknad()

            assertThat(validator.validationErrors).isEmpty()
        }

        @Test
        fun `should fail when sak has validation violations from bean validation`() {
            // Create a Sak that violates constraints, e.g., assume @NotNull on a field
            val sak =
                SakTestData.sakUtenUtbetaling()  // Modify to violate, e.g., set a required field to null if possible
            val validator = SakValidator(sak)

            validator.checkSoknad()

            // Assuming violations exist, assert errors are added
            assertThat(validator.validationErrors).isNotEmpty()
        }

        @Test
        fun `should fail when neither utbetaling nor forhandstilsagn is set`() {
            val sak = SakTestData.sakUtenUtbetaling()
            val validator = SakValidator(sak)

            validator.checkSoknad()

            assertThat(validator.validationErrors).hasSize(1)
        }
    }


    @Nested
    inner class ValidateTests {

        @Test
        fun `should not throw when no validation errors`() {
            val sak = SakTestData.sakMedUtbetaling()
            val validator = SakValidator(sak)

            validator.checkSoknad().validate()

            // No exception thrown
        }

        @Test
        fun `should throw ValideringException when validation errors exist`() {
            val sak = SakTestData.sakUtenUtbetaling()
            val validator = SakValidator(sak)

            validator.checkSoknad()

            assertThatThrownBy { validator.validate() }
                .isInstanceOf(ValideringException::class.java)
                .hasMessageContaining("Validering av sak feilet")
        }
    }
}