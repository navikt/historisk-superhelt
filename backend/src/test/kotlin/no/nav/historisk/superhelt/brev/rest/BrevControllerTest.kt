package no.nav.historisk.superhelt.brev.rest

import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.brev.*
import no.nav.historisk.superhelt.brev.pdfgen.PdfgenService
import no.nav.historisk.superhelt.person.tilgangsmaskin.TilgangsmaskinService
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakTestData
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester
import tools.jackson.databind.ObjectMapper

@MockedSpringBootTest
@AutoConfigureMockMvc
class BrevControllerTest {
    @MockitoBean
    private lateinit var pdfgenService: PdfgenService

    @MockitoBean
    private lateinit var tilgangsmaskinService: TilgangsmaskinService

    @Autowired
    private lateinit var mockMvc: MockMvcTester

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var sakRepository: SakRepository

    @Autowired
    private lateinit var brevRepository: BrevRepository

    @BeforeEach
    fun setup() {
        whenever(tilgangsmaskinService.sjekkKomplettTilgang(any())) doReturn TilgangsmaskinClient.TilgangResult(
            harTilgang = true
        )
    }


    @WithSaksbehandler
    @Nested
    inner class `hent eller opprett brev` {
        @Test
        fun `opprett brev ok`() {

            val sak = SakTestData.lagreNySak(sakRepository)
            val saksnummer = sak.saksnummer
            val request = OpprettBrevRequest(type = BrevType.VEDTAKSBREV, mottaker = BrevMottaker.BRUKER)

            assertThat(hentEllerOpprettBrev(saksnummer, request))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(Brev::class.java)
                .satisfies({
                    assertThat(it.uuid).isNotNull
                    assertThat(it.type).isEqualTo(request.type)
                    assertThat(it.type).isEqualTo(request.type)
                    assertThat(it.mottakerType).isEqualTo(request.mottaker)
                    assertThat(it.tittel).isNotEmpty
                    assertThat(it.innhold).isNotEmpty
                    assertThat(it.valideringsfeil).isNotNull
                })
            verify(tilgangsmaskinService, atLeast(1)).sjekkKomplettTilgang(sak.fnr)
        }

        @Test
        fun `hent brev om det finnes fra før`() {

            val sak = SakTestData.lagreNySak(sakRepository)
            val brev = BrevTestdata.lagreBrev(brevRepository, sak.saksnummer)
            val saksnummer = sak.saksnummer
            val request = OpprettBrevRequest(type = BrevType.VEDTAKSBREV, mottaker = BrevMottaker.BRUKER)

            assertThat(hentEllerOpprettBrev(saksnummer, request))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(Brev::class.java)
                .satisfies({
                    assertThat(it).isEqualTo(brev)
                })
            verify(tilgangsmaskinService, atLeast(1)).sjekkKomplettTilgang(sak.fnr)
        }

        @Test
        fun `ukjent sak skal gi feil`() {
            val saksnummer = Saksnummer("SUPER-999999")
            val request = OpprettBrevRequest(type = BrevType.VEDTAKSBREV, mottaker = BrevMottaker.BRUKER)
            assertThat(hentEllerOpprettBrev(saksnummer, request))
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyAsProblemDetail()
        }

        private fun hentEllerOpprettBrev(
            saksnummer: Saksnummer,
            request: OpprettBrevRequest): MockMvcTester.MockMvcRequestBuilder =
            mockMvc.post().uri("/api/sak/{saksnummer}/brev", saksnummer)
                .with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
    }

    @WithSaksbehandler
    @Nested
    inner class `hent brev` {
        @Test
        fun `hent brev ok`() {
            val sak = SakTestData.lagreNySak(sakRepository)
            val brev = BrevTestdata.lagreBrev(brevRepository, sak.saksnummer)
            val saksnummer = sak.saksnummer
            val brevId = brev.uuid

            assertThat(hentBrev(saksnummer, brevId))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(Brev::class.java)
                .satisfies({
                    assertThat(it.uuid).isEqualTo(brevId)
                })
        }

        @Test
        fun `hent brev som ikke finnes skal gi feil`() {
            val sak = SakTestData.lagreNySak(sakRepository)
            val saksnummer = sak.saksnummer
            val brevId = BrevId.random()

            assertThat(hentBrev(saksnummer, brevId))
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyAsProblemDetail()
        }

        private fun hentBrev(saksnummer: Saksnummer, brevId: BrevId): MockMvcTester.MockMvcRequestBuilder =
            mockMvc.get().uri("/api/sak/{saksnummer}/brev/{brevId}", saksnummer, brevId)
    }

    @WithSaksbehandler
    @Nested
    inner class `html brev` {
        @Test
        fun `html brev ok`() {
            val sak = SakTestData.lagreNySak(sakRepository)
            val brev = BrevTestdata.lagreBrev(brevRepository, sak.saksnummer)
            val saksnummer = sak.saksnummer
            val brevId = brev.uuid

            val html = "<html></html>"

            whenever(pdfgenService.genererHtml(any(), any())) doReturn html.toByteArray()

            assertThat(htmlBrev(saksnummer, brevId))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .isEqualTo(html)
        }

        private fun htmlBrev(saksnummer: Saksnummer, brevId: BrevId): MockMvcTester.MockMvcRequestBuilder =
            mockMvc.get().uri("/api/sak/{saksnummer}/brev/{brevId}/html", saksnummer, brevId)
    }

    @WithSaksbehandler
    @Nested
    inner class `oppdater brev` {
        @Test
        fun `oppdater brev ok`() {
            val sak = SakTestData.lagreNySak(sakRepository)
            val brev = BrevTestdata.lagreBrev(brevRepository, sak.saksnummer)
            val saksnummer = sak.saksnummer
            val brevId = brev.uuid

            val request = OppdaterBrevRequest(tittel = "Ny tittel", innhold = "Nytt innhold")

            assertThat(oppdaterBrev(saksnummer, brevId, request))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(Brev::class.java)
                .satisfies ({
                    assertThat(it.tittel).isEqualTo(request.tittel)
                    assertThat(it.innhold).isEqualTo(request.innhold)
                })
        }

        @Test
        fun `oppdater brev valideringsfeil`() {
            val sak = SakTestData.lagreNySak(sakRepository)
            val brev = BrevTestdata.lagreBrev(brevRepository, sak.saksnummer)
            val saksnummer = sak.saksnummer
            val brevId = brev.uuid

            val request = OppdaterBrevRequest(tittel = "", innhold = "Nytt innhold")

            assertThat(oppdaterBrev(saksnummer, brevId, request))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(Brev::class.java)
                .satisfies ({
                    assertThat(it.valideringsfeil).isNotEmpty
                })
        }

        @WithLeseBruker
        @Test
        fun `oppdater brev uten skrivetilgang skal gi feil`() {
            val sak = SakTestData.lagreNySak(sakRepository)
            val brev = BrevTestdata.lagreBrev(brevRepository, sak.saksnummer)
            val saksnummer = sak.saksnummer
            val brevId = brev.uuid
            val request = OppdaterBrevRequest(tittel = "Ny tittel", innhold = "Nytt innhold")
            assertThat(oppdaterBrev(saksnummer, brevId, request))
                .hasStatus4xxClientError()
                .bodyAsProblemDetail()
                .satisfies ({
                    assertThat(it?.detail).isNotBlank
                })
        }

        private fun oppdaterBrev(
            saksnummer: Saksnummer,
            brevId: BrevId,
            request: OppdaterBrevRequest): MockMvcTester.MockMvcRequestBuilder =
            mockMvc.put().uri("/api/sak/{saksnummer}/brev/{brevId}", saksnummer, brevId)
                .with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
    }
}