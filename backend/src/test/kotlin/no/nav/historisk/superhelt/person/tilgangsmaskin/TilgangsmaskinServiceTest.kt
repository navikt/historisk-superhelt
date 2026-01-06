package no.nav.historisk.superhelt.person.tilgangsmaskin

import no.nav.common.types.Fnr
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.tilgangsmaskin.TilgangsmaskinClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.*
import org.springframework.cache.Cache
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
class TilgangsmaskinServiceTest {

    @MockitoBean
    private lateinit var tilgangsmaskinClient: TilgangsmaskinClient

    @MockitoBean
    private lateinit var cache: Cache

    private lateinit var service: TilgangsmaskinService

    @BeforeEach
    fun setUp() {
        service = TilgangsmaskinService(tilgangsmaskinClient, cache)
    }

    @WithSaksbehandler(navIdent = "Z999999")
    @Test
    fun `should return from cache if present and not call client`() {
        val fnr = Fnr("12345678901")
        val expectedResult = TilgangsmaskinClient.TilgangResult(true)

        whenever(cache.get(any(), eq(TilgangsmaskinClient.TilgangResult::class.java)))
            .thenReturn(expectedResult)

        val result = service.sjekkKomplettTilgang(fnr)

        assertEquals(expectedResult, result)
        verify(tilgangsmaskinClient, never()).komplett(any())
        verify(cache).get(eq("Z999999:12345678901"), eq(TilgangsmaskinClient.TilgangResult::class.java))
        verify(cache, never()).put(any(), any())
    }

    @WithSaksbehandler(navIdent = "Z888888")
    @Test
    fun `should call client`() {
        val fnr = Fnr("12345678901")
        val expectedResult = TilgangsmaskinClient.TilgangResult(true)

        whenever(cache.get(any(), eq(TilgangsmaskinClient.TilgangResult::class.java)))
            .thenReturn(null)
        whenever(tilgangsmaskinClient.komplett(any()))
            .thenReturn(expectedResult)

        val result = service.sjekkKomplettTilgang(fnr)

        assertEquals(expectedResult, result)
        verify(tilgangsmaskinClient).komplett(any())
        verify(cache).get(eq("Z888888:12345678901"), eq(TilgangsmaskinClient.TilgangResult::class.java))
        verify(cache).put(any(), any())
    }

}


