package no.nav.historisk.superhelt.vedtak

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VedtaksResultatTest {

    @Test
    fun `INNVILGET er innvilget`() {
        assertThat(VedtaksResultat.INNVILGET.isInnvilget()).isTrue()
    }

    @Test
    fun `DELVIS_INNVILGET er innvilget`() {
        assertThat(VedtaksResultat.DELVIS_INNVILGET.isInnvilget()).isTrue()
    }

    @Test
    fun `AVSLATT er ikke innvilget`() {
        assertThat(VedtaksResultat.AVSLATT.isInnvilget()).isFalse()
    }

    @Test
    fun `HENLAGT er ikke innvilget`() {
        assertThat(VedtaksResultat.HENLAGT.isInnvilget()).isFalse()
    }
}
