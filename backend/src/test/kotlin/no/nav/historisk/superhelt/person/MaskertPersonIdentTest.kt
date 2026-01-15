package no.nav.historisk.superhelt.person

import no.nav.common.types.FolkeregisterIdent
import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MaskertPersonIdentTest {

    @Test
    fun `skal kryptere og dekryptere gyldig fnr`() {
        val fnr = FolkeregisterIdent("12345678901")

        val maskert = fnr.toMaskertPersonIdent()
        val dekryptert = maskert.toFnr()

        assertThat(dekryptert).isEqualTo(fnr)
    }

    @Test
    fun `kryptert verdi skal være forskjellig fra original fnr`() {
        val fnr = FolkeregisterIdent("12345678901")

        val maskert = fnr.toMaskertPersonIdent()

        assertThat(maskert.value).isNotEqualTo(fnr.value)
    }


    @Test
    fun `skal kaste IkkeFunnetException ved dekryptering av ugyldig maskert verdi`() {
        val ugyldigMaskert = MaskertPersonIdent("ugyldig_verdi_123")

        assertThrows<IkkeFunnetException> {
            ugyldigMaskert.toFnr()
        }
    }

    @Test
    fun `forskjellige fnr skal gi forskjellige krypterte verdier`() {
        val fnr1 = FolkeregisterIdent("12345678901")
        val fnr2 = FolkeregisterIdent("10987654321")

        val maskert1 = fnr1.toMaskertPersonIdent()
        val maskert2 = fnr2.toMaskertPersonIdent()

        assertThat(maskert1.value).isNotEqualTo(maskert2.value)
    }
}
