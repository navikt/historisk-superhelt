package no.nav.historisk.superhelt.sak.rest

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.historisk.superhelt.person.TilgangsmaskinTestData
import no.nav.historisk.superhelt.person.tilgangsmaskin.TilgangsmaskinService
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.StonadsType
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.bodyAsProblemDetail
import no.nav.person.Fnr
import no.nav.tilgangsmaskin.TilgangsmaskinClient
import org.assertj.core.api.Assertions.assertThat
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
@WithMockUser(authorities = ["READ", "WRITE"])
class SakControllerRestTest() {

    @Autowired
    private lateinit var mockMvc: MockMvcTester

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var repository: SakRepository

    @Autowired
    private lateinit var tilgangsmaskinService: TilgangsmaskinService

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
}



