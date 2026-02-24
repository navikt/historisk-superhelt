package no.nav.historisk.superhelt.sak

import no.nav.common.types.Behandlingsnummer
import no.nav.historisk.superhelt.infrastruktur.validation.ValideringException
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
        fun `should allow invalid transition from TIL_ATTESTERING to UNDER_BEHANDLING`() {
            val sak = SakTestData.sakUtenUtbetaling().copy(status = SakStatus.TIL_ATTESTERING)
            val validator = SakValidator(sak)

            validator.checkStatusTransition(SakStatus.UNDER_BEHANDLING)
            assertThat(validator.validationErrors).isEmpty()
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
            val sak = SakTestData.sakMedUtbetaling().copy(beskrivelse = null)
            val validator = SakValidator(sak)

            validator.checkSoknad()

            assertThatThrownBy { validator.validate() }
                .isInstanceOf(ValideringException::class.java)
                .hasMessageContaining("Validering av sak feilet")
        }
    }

    @Nested
    inner class UpdateTests {

        @Test
        fun `skal kunne oppdatere type på første versjon av sak`() {
            val sak = SakTestData.sakMedUtbetaling().copy(type = StonadsType.SPESIALSKO,behandlingsnummer = Behandlingsnummer(1))
            val validator = SakValidator(sak)
            assertThat(sak.gjenapnet).isFalse()

            validator.checkUpdate(UpdateSakDto(type = StonadsType.BRYSTPROTESE)).validate()
            // No exception thrown
        }
        @Test
        fun `skal kunne oppdatere til samme type på en gjenåpnet sak`() {
            val sak = SakTestData.sakMedUtbetaling().copy(type = StonadsType.SPESIALSKO,behandlingsnummer = Behandlingsnummer(2))
            val validator = SakValidator(sak)
            assertThat(sak.gjenapnet).isTrue()

            validator.checkUpdate(UpdateSakDto(type = StonadsType.SPESIALSKO)).validate()
        }

        @Test
        fun `skal kunne oppdatere andre ting enn type på gjenåpnet sak`() {
            val sak = SakTestData.sakMedUtbetaling().copy(type = StonadsType.SPESIALSKO,behandlingsnummer = Behandlingsnummer(2))
            val validator = SakValidator(sak)
            assertThat(sak.gjenapnet).isTrue()

            validator.checkUpdate(UpdateSakDto(beskrivelse = "hei hallo")).validate()
        }


        @Test
        fun `skal ikke kunne oppdatere type på en gjenåpnet sak`() {
            val sak = SakTestData.sakMedUtbetaling().copy(type = StonadsType.SPESIALSKO,behandlingsnummer = Behandlingsnummer(2))
            val validator = SakValidator(sak)
            assertThat(sak.gjenapnet).isTrue()

            validator.checkUpdate(UpdateSakDto(type = StonadsType.BRYSTPROTESE))

            assertThatThrownBy { validator.validate() }
                .isInstanceOf(ValideringException::class.java)
                .hasMessageContaining("Validering av sak feilet")
            // No exception thrown
        }
    }


}