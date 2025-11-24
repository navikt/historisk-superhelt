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
        fun `should allow valid transition from UNDER_BEHANDLING to TIL_ATTESTERING`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(status = SakStatus.UNDER_BEHANDLING)
            val validator = SakValidator(sak)

            validator.checkStatusTransition(SakStatus.TIL_ATTESTERING)

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
            val sak = SakTestData.sakMedUtbetaling().copy(tittel = null)
            val validator = SakValidator(sak)

            validator.checkSoknad()

            assertThatThrownBy { validator.validate() }
                .isInstanceOf(ValideringException::class.java)
                .hasMessageContaining("Validering av sak feilet")
        }
    }
}