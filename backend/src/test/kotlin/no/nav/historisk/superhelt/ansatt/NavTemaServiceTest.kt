package no.nav.historisk.superhelt.ansatt

import no.nav.common.consts.EksternFellesKodeverkTema
import no.nav.entraproxy.EntraProxyClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class NavTemaServiceTest {

    private val entraProxyClient: EntraProxyClient = mock()
    private val service = NavAnsattService(entraProxyClient)

    @Test
    fun `hentNavTema returnerer sett med tema fra entra-proxy`() {
        whenever(entraProxyClient.hentTema()).thenReturn(setOf("HJE", "ORT", "AAP", "HEL"))

        val result = service.hentNavTema()

        assertThat(result).containsExactlyInAnyOrder(EksternFellesKodeverkTema.HJE, EksternFellesKodeverkTema.HEL)
    }

    @Test
    fun `hentNavTema returnerer tomt sett`() {
        whenever(entraProxyClient.hentTema()).thenReturn(emptySet())

        val result = service.hentNavTema()

        assertThat(result).isEmpty()
    }

    @Test
    fun `hentNavTema propagerer exception fra entra-proxy`() {
        whenever(entraProxyClient.hentTema()).thenThrow(RuntimeException("entra-proxy nede"))

        assertThatThrownBy { service.hentNavTema() }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessageContaining("entra-proxy nede")
    }
}
