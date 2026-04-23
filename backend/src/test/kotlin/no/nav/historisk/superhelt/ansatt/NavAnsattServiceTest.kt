package no.nav.historisk.superhelt.ansatt

import no.nav.common.types.Enhetsnummer
import no.nav.entraproxy.Enhet
import no.nav.entraproxy.EntraProxyClient
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class NavAnsattServiceTest {

    private val entraProxyClient: EntraProxyClient = mock()
    private val service = NavAnsattService(entraProxyClient)

    @Test
    fun `hentNavEnhet returnerer enhetnummer for første enhet i listen`() {
        whenever(entraProxyClient.hentEnheter()).thenReturn(
            listOf(
                Enhet(enhetnummer = Enhetsnummer("4488"), navn = "NAV Vest-Viken"),
                Enhet(enhetnummer = Enhetsnummer("0300"), navn = "NAV Oslo"),
            )
        )

        val result = service.hentNavEnheter()

        Assertions.assertThat(result).containsExactlyInAnyOrder(
            Enhet(enhetnummer = Enhetsnummer("4488"), navn = "NAV Vest-Viken"),
            Enhet(enhetnummer = Enhetsnummer("0300"), navn = "NAV Oslo"),
        )
    }


    @Test
    fun `hentNavEnhet propagerer exception fra entra-proxy`() {
        whenever(entraProxyClient.hentEnheter()).thenThrow(RuntimeException("entra-proxy nede"))

        Assertions.assertThatThrownBy { service.hentNavEnheter() }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessageContaining("entra-proxy nede")
    }
}
