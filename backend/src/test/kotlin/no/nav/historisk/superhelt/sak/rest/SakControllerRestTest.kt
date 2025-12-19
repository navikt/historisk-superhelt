package no.nav.historisk.superhelt.sak.rest

import no.nav.common.types.Fnr
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.person.TilgangsmaskinTestData
import no.nav.historisk.superhelt.person.tilgangsmaskin.TilgangsmaskinService
import no.nav.historisk.superhelt.person.toMaskertPersonIdent
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.sak.StonadsType
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.WithLeseBruker
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.historisk.superhelt.test.bodyAsProblemDetail
import no.nav.tilgangsmaskin.TilgangsmaskinClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester
import tools.jackson.databind.ObjectMapper

@MockedSpringBootTest
@AutoConfigureMockMvc
class SakControllerRestTest() {

    @Autowired
    private lateinit var mockMvc: MockMvcTester

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var repository: SakRepository

    @MockitoBean
    private lateinit var tilgangsmaskinService: TilgangsmaskinService

    @BeforeEach
    fun setup() {
        whenever(tilgangsmaskinService.sjekkKomplettTilgang(any())) doReturn TilgangsmaskinClient.TilgangResult(
            harTilgang = true
        )
    }

    @WithSaksbehandler
    @Nested
    inner class `opprett sak` {
        @Test
        fun `opprett sak ok`() {
            val fnr = Fnr("22345678901")
            assertThat(opprettSak(fnr))
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .convertTo(Sak::class.java)
                .satisfies({
                    assertThat(it.fnr).isEqualTo(fnr)
                    assertThat(it.type).isEqualTo(StonadsType.BRYSTPROTESE)
                    assertThat(it.saksnummer).isNotNull
                })
            verify(tilgangsmaskinService, atLeast(1)).sjekkKomplettTilgang(fnr)
        }

        @WithLeseBruker
        @Test
        fun `opprett sak uten skrivetilgang skal gi feil`() {
            val fnr = Fnr("32345678901")
            assertThat(opprettSak(fnr))
                .hasStatus(HttpStatus.FORBIDDEN)
                .bodyAsProblemDetail()
                .satisfies({
                    assertThat(it?.detail).isNotBlank
                })
        }

        @Test
        fun `opprett sak uten rettighet for person skal gi feil`() {
            val fnr = Fnr("42345678901")
            whenever(tilgangsmaskinService.sjekkKomplettTilgang(fnr)) doReturn TilgangsmaskinClient.TilgangResult(
                harTilgang = false,
                TilgangsmaskinTestData.problemDetailResponse,
            )
            assertThat(opprettSak(fnr))
                .hasStatus(HttpStatus.FORBIDDEN)
                .bodyAsProblemDetail()
        }

        private fun opprettSak(fnr: Fnr): MockMvcTester.MockMvcRequestBuilder = mockMvc.post().uri("/api/sak")
            .with(csrf())
            .contentType("application/json")
            .content(
                objectMapper.writeValueAsString(
                    SakCreateRequestDto(
                        type = StonadsType.BRYSTPROTESE,
                        fnr = fnr
                    )
                )
            )
    }

    @WithSaksbehandler
    @Nested
    inner class `oppdater sak` {

        @Test
        fun `oppdater sak ok`() {
            val opprettetSak = SakTestData.lagreNySak(repository, SakTestData.nySakMinimum())
            val saksnummer = opprettetSak.saksnummer
            val oppdatertTittel = "Ny tittel"
            val oppdatertBegrunnelse = "Ny begrunnelse"

            assertThat(
                oppdaterSak(
                    saksnummer, SakUpdateRequestDto(
                        tittel = oppdatertTittel,
                        begrunnelse = oppdatertBegrunnelse
                    )
                )
            )
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(Sak::class.java)
                .satisfies({
                    assertThat(it.saksnummer).isEqualTo(saksnummer)
                    assertThat(it.tittel).isEqualTo(oppdatertTittel)
                    assertThat(it.begrunnelse).isEqualTo(oppdatertBegrunnelse)
                    assertThat(it.fnr).isNotNull
                })
            verify(tilgangsmaskinService, atLeast(1)).sjekkKomplettTilgang(opprettetSak.fnr)
        }

        @WithLeseBruker
        @Test
        fun `oppdater sak uten skrivetilgang skal gi feil`() {
            val opprettetSak = SakTestData.lagreNySak(repository, SakTestData.nySakMinimum()).saksnummer
            assertThat(
                oppdaterSak(
                    opprettetSak, SakUpdateRequestDto(
                        tittel = "Ny tittel",
                        begrunnelse = "Ny begrunnelse"
                    )
                )
            )
                .hasStatus4xxClientError()
                .bodyAsProblemDetail()
                .satisfies({
                    assertThat(it?.detail).isNotBlank
                })
        }

        @Test
        fun `oppdater sak som ikke finnes skal gi feil`() {
            val ikkeFinnsSaksnummer = Saksnummer("SUPER-999999")

            assertThat(
                oppdaterSak(
                    ikkeFinnsSaksnummer, SakUpdateRequestDto(
                        tittel = "Ny tittel",
                        begrunnelse = "Ny begrunnelse"
                    )
                )
            )
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyAsProblemDetail()
        }

        private fun oppdaterSak(
            saksnummer: Saksnummer?,
            dto: SakUpdateRequestDto
        ): MockMvcTester.MockMvcRequestBuilder = mockMvc.put().uri("/api/sak/{saksnummer}", saksnummer)
            .with(csrf())
            .contentType("application/json")
            .content(
                objectMapper.writeValueAsString(dto)
            )
    }


    @WithSaksbehandler
    @Nested
    inner class `hent sak` {

        @Test
        fun `hent sak ok sjekk json`() {
            val opprettetSak = SakTestData.lagreNySak(repository, SakTestData.nySakMinimum())

            assertThat(hentSak(opprettetSak.saksnummer))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPathSatisfying("$.saksnummer") { assertThat(it).isEqualTo(opprettetSak.saksnummer.value) }
                .hasPathSatisfying("$.fnr") { assertThat(it).isEqualTo(opprettetSak.fnr.value) }
                .hasPathSatisfying("$.type") { assertThat(it).isEqualTo(opprettetSak.type.name) }
                .hasPathSatisfying("$.tittel") { assertThat(it).isEqualTo(opprettetSak.tittel) }
                // Genererte verdier
                .hasPathSatisfying("$.maskertPersonIdent") { assertThat(it).isEqualTo(opprettetSak.fnr.toMaskertPersonIdent().value) }
                .hasPathSatisfying("$.rettigheter") { assertThat(it).isNotEmpty }
                .hasPathSatisfying("$.tilstand") { assertThat(it).isNotEmpty }
                .hasPathSatisfying("$.valideringsfeil") { assertThat(it).isNotNull }


            verify(tilgangsmaskinService, atLeast(1)).sjekkKomplettTilgang(opprettetSak.fnr)
        }

        @Test
        fun `hent sak som ikke finnes skal gi feil`() {
            val ikkeFinnsSaksnummer = Saksnummer("SUPER-999999")

            assertThat(hentSak(ikkeFinnsSaksnummer))
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyAsProblemDetail()
        }

        @Test
        fun `hent sak uten tilgang til person skal gi feil`() {
            val opprettetSak = SakTestData.lagreNySak(repository, SakTestData.nySakMinimum())
            whenever(tilgangsmaskinService.sjekkKomplettTilgang(opprettetSak.fnr)) doReturn TilgangsmaskinClient.TilgangResult(
                harTilgang = false,
                TilgangsmaskinTestData.problemDetailResponse,
            )

            assertThat(hentSak(opprettetSak.saksnummer))
                .hasStatus(HttpStatus.FORBIDDEN)
                .bodyAsProblemDetail()
        }

        private fun hentSak(saksnummer: Saksnummer?): MockMvcTester.MockMvcRequestBuilder =
            mockMvc.get().uri("/api/sak/{saksnummer}", saksnummer)
    }

    @WithSaksbehandler
    @Nested
    inner class `finn saker for person` {


        @Test
        fun `finn saker for person ok`() {
            val fnr = Fnr("12345678901")
            SakTestData.lagreNySak(repository, SakTestData.nySakMinimum(fnr))
            SakTestData.lagreNySak(repository, SakTestData.nySakMinimum(fnr))
            SakTestData.lagreNySak(repository, SakTestData.nySakMinimum(Fnr("98765432101")))

            assertThat(finnSakerForPerson(fnr))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(List::class.java)
                .satisfies({
                    assertThat(it).hasSize(2)
                })

            verify(tilgangsmaskinService, atLeast(1)).sjekkKomplettTilgang(fnr)
        }

        @WithMockUser()
        @Test
        fun `finn saker for saksbehandler uten lesetilgang skal gi feil`() {
            val fnr = Fnr("22345678901")
            SakTestData.lagreNySak(repository, SakTestData.nySakMinimum(fnr))
            assertThat(finnSakerForPerson(fnr))
                .hasStatus(HttpStatus.FORBIDDEN)

        }

        @Test
        fun `finn saker for saksbehandler uten rettighet for person skal gi feil`() {
            val fnr = Fnr("32345678901")
            SakTestData.lagreNySak(repository, SakTestData.nySakMinimum(fnr))
            whenever(tilgangsmaskinService.sjekkKomplettTilgang(fnr)) doReturn TilgangsmaskinClient.TilgangResult(
                harTilgang = false,
                TilgangsmaskinTestData.problemDetailResponse,
            )
            assertThat(finnSakerForPerson(fnr))
                .hasStatus(HttpStatus.FORBIDDEN)
                .bodyAsProblemDetail()
        }

        private fun finnSakerForPerson(fnr: Fnr): MockMvcTester.MockMvcRequestBuilder =
            mockMvc.get().uri("/api/sak")
                .queryParam("maskertPersonId", fnr.toMaskertPersonIdent().value)
    }
}
