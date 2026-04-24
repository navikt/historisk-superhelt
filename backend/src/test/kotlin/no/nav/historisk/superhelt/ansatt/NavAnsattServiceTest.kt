package no.nav.historisk.superhelt.ansatt

import no.nav.common.consts.FellesKodeverkTema
import no.nav.entraproxy.EntraProxyClient
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.historisk.superhelt.test.withMockedUser
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension


@WithSaksbehandler
@ExtendWith(SpringExtension::class)
class NavAnsattServiceTest {

    @MockitoBean
    private lateinit var entraProxyClient: EntraProxyClient

    private val cacheManager = ConcurrentMapCacheManager(NAVANSATT_CACHE)

    private lateinit var service: NavAnsattService

    @BeforeEach
    fun setUp() {
        cacheManager.getCache(NAVANSATT_CACHE)?.clear()
        service = NavAnsattService(entraProxyClient, cacheManager)
    }

    @Test
    fun `hentNavAnsatt returnerer enheter og tema`() {
        val enheter = NavAnsattTestdata.createEnheter(2)
        whenever(entraProxyClient.hentEnheter()).thenReturn(enheter)
        whenever(entraProxyClient.hentTema()).thenReturn(setOf("HJE", "ORT", "AAP", "HEL"))

        val result = service.hentNavAnsatt()

        assertThat(result.enheter).isEqualTo(enheter)
        assertThat(result.tema).containsExactlyInAnyOrder(FellesKodeverkTema.HJE, FellesKodeverkTema.HEL)
    }

    @Test
    fun `hentNavAnsatt returnerer tomt sett når entra-proxy ikke returnerer data`() {
        whenever(entraProxyClient.hentEnheter()).thenReturn(emptyList())
        whenever(entraProxyClient.hentTema()).thenReturn(emptySet())

        val result = service.hentNavAnsatt()

        assertThat(result.enheter).isEmpty()
        assertThat(result.tema).isEmpty()
    }

    @Test
    fun `hentNavAnsatt cacher kall mot entra-proxy per bruker`() {
        whenever(entraProxyClient.hentEnheter()).thenReturn(NavAnsattTestdata.createEnheter(1))
        whenever(entraProxyClient.hentTema()).thenReturn(setOf("HJE"))

        service.hentNavAnsatt()
        service.hentNavAnsatt()
        service.hentNavAnsatt()

        verify(entraProxyClient, times(1)).hentEnheter()
        verify(entraProxyClient, times(1)).hentTema()

        // Kjører som en annen bruker
        withMockedUser("X888888"){
            service.hentNavAnsatt()
            service.hentNavAnsatt()
            service.hentNavAnsatt()

            verify(entraProxyClient, times(2)).hentEnheter()
            verify(entraProxyClient, times(2)).hentTema()
        }
    }

    @Test
    fun `hentNavAnsatt propagerer exception fra entra-proxy`() {
        whenever(entraProxyClient.hentEnheter()).thenThrow(RuntimeException("entra-proxy nede"))

        assertThatThrownBy { service.hentNavAnsatt() }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessageContaining("entra-proxy nede")
    }
}
