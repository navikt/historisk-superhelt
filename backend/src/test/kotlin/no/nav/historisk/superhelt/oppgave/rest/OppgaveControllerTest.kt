package no.nav.historisk.superhelt.oppgave.rest

import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.oppgave.OppgaveTestdata
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.oppgave.OppgaveClient
import no.nav.oppgave.model.SokOppgaverResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester
import tools.jackson.databind.ObjectMapper

@MockedSpringBootTest
@AutoConfigureMockMvc
class OppgaveControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvcTester

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var oppgaveClient: OppgaveClient
//    @MockitoBean
//    private lateinit var tilgangsmaskinService: TilgangsmaskinService


//    @BeforeEach
//    fun setup() {
//        whenever(tilgangsmaskinService.sjekkKomplettTilgang(any())) doReturn TilgangsmaskinClient.TilgangResult(
//            harTilgang = true
//        )
//    }

    @WithSaksbehandler(navIdent = "Z123456")
    @Test
    fun `Hent oppgaver for saksbehandler`() {
        whenever(oppgaveClient.finnOppgaver(any())) doReturn SokOppgaverResponse(
            antallTreffTotalt = 2,
            oppgaver = listOf(
                OppgaveTestdata.opprettOppgave(),
                OppgaveTestdata.opprettOppgave()
            )
        )

        assertThat(mockMvc.get().uri("/api/oppgave/saksbehandler"))
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .convertTo(List::class.java)
            .satisfies({
                assertThat(it).hasSize(2)
            })
    }

    private fun hentSak(saksnummer: Saksnummer?): MockMvcTester.MockMvcRequestBuilder =
        mockMvc.get().uri("/api/oppgave/saksbehandler")


}