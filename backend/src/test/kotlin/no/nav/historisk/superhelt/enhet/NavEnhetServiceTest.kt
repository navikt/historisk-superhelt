package no.nav.historisk.superhelt.enhet

import no.nav.common.types.Enhetsnummer
import no.nav.entraproxy.Enhet
import no.nav.entraproxy.EntraProxyClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class NavEnhetServiceTest {

    private val entraProxyClient: EntraProxyClient = mock()
    private val service = NavEnhetService(entraProxyClient)

    @Test
    fun `hentNavEnhet returnerer enhetnummer for første enhet i listen`() {
        whenever(entraProxyClient.hentEnheter()).thenReturn(
            listOf(
                Enhet(enhetnummer = "4488", navn = "NAV Vest-Viken"),
                Enhet(enhetnummer = "0300", navn = "NAV Oslo"),
            )
        )

        val result = service.hentNavEnhet()

        assertThat(result).isEqualTo(Enhetsnummer("4488"))
    }

    @Test
    fun `hentNavEnhet kaster exception når bruker ikke tilhører noen enhet`() {
        whenever(entraProxyClient.hentEnheter()).thenReturn(emptyList())

        assertThatThrownBy { service.hentNavEnhet() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("enhet")
    }

    @Test
    fun `hentNavEnhet propagerer exception fra entra-proxy`() {
        whenever(entraProxyClient.hentEnheter()).thenThrow(RuntimeException("entra-proxy nede"))

        assertThatThrownBy { service.hentNavEnhet() }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessageContaining("entra-proxy nede")
    }
}
