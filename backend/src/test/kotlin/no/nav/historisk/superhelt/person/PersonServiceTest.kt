package no.nav.historisk.superhelt.person

import no.nav.common.types.Fnr
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.pdl.*
import no.nav.pdl.Person
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(SpringExtension::class)
class PersonServiceTest {

    @MockitoBean
    private lateinit var pdlClient: PdlClient

    private val cacheManager = ConcurrentMapCacheManager("pdlCache")

    private lateinit var service: PersonService

    @BeforeEach
    fun setUp() {
        cacheManager.getCache("pdlCache")?.clear()
        service = PersonService(pdlClient, cacheManager)
    }

    @WithSaksbehandler
    @Test
    fun `skal hente fra PDL når cache er tom`() {
        val fnr = Fnr("12345678901")
        val pdlResponse = createMockPdlResponse()

        whenever(pdlClient.getPersonOgIdenter(fnr.value)).thenReturn(pdlResponse)

        val result = service.hentPerson(fnr)

        assertNotNull(result)
        verify(pdlClient, times(1)).getPersonOgIdenter(fnr.value)
    }

    @WithSaksbehandler
    @Test
    fun `skal hente fra cache ved andre kall`() {
        val fnr = Fnr("12345678901")
        val pdlResponse = createMockPdlResponse()

        whenever(pdlClient.getPersonOgIdenter(fnr.value)).thenReturn(pdlResponse)

        val firstResult = service.hentPerson(fnr)
        val secondResult = service.hentPerson(fnr)

        assertNotNull(firstResult)
        assertNotNull(secondResult)
        assertEquals(firstResult, secondResult)
        verify(pdlClient, times(1)).getPersonOgIdenter(fnr.value)
    }

    private fun createMockPdlResponse(): HentPdlResponse {
        return HentPdlResponse(
            data = PdlData(
                hentPerson = Person(
                    navn = listOf(Navn("Ola", null, "Nordmann")),
                    doedsfall = null,
                    adressebeskyttelse = null,
                    vergemaalEllerFremtidsfullmakt = null
                ),
                hentIdenter = Identliste(
                    identer = listOf(
                        IdentInformasjon("12345678901", IdentGruppe.FOLKEREGISTERIDENT, false),
                        IdentInformasjon("10987654321111", IdentGruppe.AKTORID, false),
                    )
                )
            ),
            errors = null
        )
    }
}
