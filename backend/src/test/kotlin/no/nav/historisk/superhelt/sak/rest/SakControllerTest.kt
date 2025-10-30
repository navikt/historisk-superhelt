package no.nav.historisk.superhelt.sak.rest

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.historisk.superhelt.person.TilgangsmaskinTestData
import no.nav.historisk.superhelt.person.tilgangsmaskin.TilgangsmaskinService
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.Saksnummer
import no.nav.historisk.superhelt.sak.StonadsType
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.bodyAsProblemDetail
import no.nav.person.Fnr
import no.nav.tilgangsmaskin.TilgangsmaskinClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.assertj.MockMvcTester

@MockedSpringBootTest
@AutoConfigureMockMvc

class SakControllerRestTest() {

    @Autowired
    private lateinit var mockMvc: MockMvcTester

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var repository: SakRepository

    @Autowired
    private lateinit var tilgangsmaskinService: TilgangsmaskinService

    @WithMockUser(authorities = ["READ", "WRITE"])
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
        }

        @WithMockUser(authorities = ["READ"])
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
    @WithMockUser(authorities = ["READ", "WRITE"])
    @Nested
    inner class `oppdater sak` {

        @Test
        fun `oppdater sak ok`() {
            val  opprettetSak = repository.save(SakTestData.sakEntityMinimum).saksnummer
            val oppdatertTittel = "Ny tittel"
            val oppdatertBegrunnelse = "Ny begrunnelse"

            assertThat(
                oppdaterSak(
                    opprettetSak, SakUpdateRequestDto(
                        tittel = oppdatertTittel,
                        begrunnelse = oppdatertBegrunnelse
                    )
                )
            )
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(Sak::class.java)
                .satisfies({
                    assertThat(it.saksnummer).isEqualTo(opprettetSak)
                    assertThat(it.tittel).isEqualTo(oppdatertTittel)
                    assertThat(it.begrunnelse).isEqualTo(oppdatertBegrunnelse)
                    assertThat(it.fnr).isNotNull
                })
        }

        @WithMockUser(authorities = ["READ"])
        @Test
        fun `oppdater sak uten skrivetilgang skal gi feil`() {
            val  opprettetSak = Saksnummer("SUPER-000001")
            assertThat(
                oppdaterSak(
                    opprettetSak, SakUpdateRequestDto(
                        tittel = "Ny tittel",
                        begrunnelse = "Ny begrunnelse"
                    )
                )
            )
                .hasStatus(HttpStatus.FORBIDDEN)
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
}



