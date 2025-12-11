package no.nav.historisk.superhelt.brev

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BrevValidatorTest {

    @Nested
    inner class CheckBrev {

        @Test
        fun `checkBrev has no errors when tittel and innhold are valid`() {
            val brev = BrevTestdata.vedtaksbrevBruker()
            val validator = BrevValidator(brev).checkBrev()
            assertThat(validator.validationErrors).isEmpty()
        }

        @Test
        fun `checkBrev has error for tittel when tittel is null`() {
            val brev = BrevTestdata.vedtaksbrevBruker().copy(tittel = null)
            val validator = BrevValidator(brev).checkBrev()
            assertThat(validator.validationErrors).isNotEmpty()
            assertThat(validator.validationErrors).anyMatch { it.field == "tittel" && it.message == "Vedtaksbrev til bruker må ha en tittel" }
        }

        @Test
        fun `checkBrev has error for tittel when tittel is blank`() {
            val brev = BrevTestdata.vedtaksbrevBruker().copy(tittel = "")
            val validator = BrevValidator(brev).checkBrev()
            assertThat(validator.validationErrors).isNotEmpty()
            assertThat(validator.validationErrors).anyMatch { it.field == "tittel" && it.message == "Vedtaksbrev til bruker må ha en tittel" }
        }

        @Test
        fun `checkBrev has error for innhold when innhold is empty HTML`() {
            val brev = BrevTestdata.vedtaksbrevBruker().copy(innhold = "")
            val validator = BrevValidator(brev).checkBrev()
            assertThat(validator.validationErrors).isNotEmpty()
            assertThat(validator.validationErrors).anyMatch { it.field == "innhold" && it.message == "Vedtaksbrev til bruker må ha innhold" }
        }

        @Test
        fun `checkBrev has error for innhold when innhold is only HTML tags`() {
            val brev = BrevTestdata.vedtaksbrevBruker().copy(innhold = "<p></p>")
            val validator = BrevValidator(brev).checkBrev()
            assertThat(validator.validationErrors).isNotEmpty()
            assertThat(validator.validationErrors).anyMatch { it.field == "innhold" && it.message == "Vedtaksbrev til bruker må ha innhold" }
        }

        @Test
        fun `checkBrev has multiple errors when both tittel and innhold are invalid`() {
            val brev = BrevTestdata.vedtaksbrevBruker().copy(tittel = "", innhold = "")
            val validator = BrevValidator(brev).checkBrev()
            assertThat(validator.validationErrors).isNotEmpty()
            assertThat(validator.validationErrors).hasSize(2)
            assertThat(validator.validationErrors).anyMatch { it.field == "tittel" }
            assertThat(validator.validationErrors).anyMatch { it.field == "innhold" }
        }
    }
}
