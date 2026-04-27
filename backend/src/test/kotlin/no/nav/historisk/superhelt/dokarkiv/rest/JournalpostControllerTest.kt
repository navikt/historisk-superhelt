package no.nav.historisk.superhelt.dokarkiv.rest

import net.datafaker.Faker
import no.nav.common.consts.APP_NAVN
import no.nav.common.types.EksternJournalpostId
import no.nav.dokarkiv.EksternDokumentInfoId
import no.nav.historisk.superhelt.dokarkiv.DokarkivTestdata
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.historisk.superhelt.test.bodyAsProblemDetail
import no.nav.saf.graphql.DokumentoversiktBrukerData
import no.nav.saf.graphql.DokumentoversiktBrukerGraphqlResponse
import no.nav.saf.graphql.DokumentoversiktFagsakData
import no.nav.saf.graphql.DokumentoversiktFagsakGraphqlResponse
import no.nav.saf.graphql.DokumentoversiktResult
import no.nav.saf.graphql.HentJournalpostData
import no.nav.saf.graphql.HentJournalpostGraphqlResponse
import no.nav.saf.graphql.SafGraphqlClient
import no.nav.saf.rest.DokumentResponse
import no.nav.saf.rest.SafRestClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester

@MockedSpringBootTest
@AutoConfigureMockMvc
@WithSaksbehandler
class JournalpostControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvcTester

    @MockitoBean
    private lateinit var safGraphqlClient: SafGraphqlClient

    @MockitoBean
    private lateinit var safRestClient: SafRestClient

    @Autowired
    private lateinit var sakRepository: SakRepository

    private val faker = Faker()

    @Nested
    inner class LastNedEttDokument {

        @Test
        fun `returnerer 200 OK med PDF-innhold når journalpost og dokument finnes`() {
            val journalpost = DokarkivTestdata.journalPost()
            val journalpostId = journalpost.journalpostId
            val dokumentId = journalpost.dokumenter!!.first().dokumentInfoId
            val pdfBytes = "test-pdf-innhold".toByteArray()

            whenever(safGraphqlClient.hentJournalpost(any()))
                .thenReturn(HentJournalpostGraphqlResponse(data = HentJournalpostData(journalpost)))
            whenever(safRestClient.hentDokument(any(), any()))
                .thenReturn(DokumentResponse(
                    data = pdfBytes,
                    contentType = MediaType.APPLICATION_PDF,
                    fileName = "dokument.pdf",
                    contentLength = pdfBytes.size.toLong()
                ))

            assertThat(
                mockMvc.get()
                    .uri("/api/journalpost/{journalpostId}/{dokumentId}", journalpostId, dokumentId)
            )
                .hasStatus(HttpStatus.OK)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PDF)
        }

        @Test
        fun `returnerer 404 når journalpost ikke finnes i SAF`() {
            val journalpostId = EksternJournalpostId(faker.numerify("########"))
            val dokumentId = EksternDokumentInfoId(faker.numerify("#########"))

            whenever(safGraphqlClient.hentJournalpost(any()))
                .thenReturn(HentJournalpostGraphqlResponse(data = null))

            assertThat(
                mockMvc.get()
                    .uri("/api/journalpost/{journalpostId}/{dokumentId}", journalpostId, dokumentId)
            )
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyAsProblemDetail()
                .extracting("status")
                .isEqualTo(404)
        }

        @Test
        fun `returnerer 404 når dokument ikke finnes i journalposten`() {
            val journalpost = DokarkivTestdata.journalPost()
            val journalpostId = journalpost.journalpostId
            val ukjentDokumentId = EksternDokumentInfoId(faker.numerify("#########"))

            whenever(safGraphqlClient.hentJournalpost(any()))
                .thenReturn(HentJournalpostGraphqlResponse(data = HentJournalpostData(journalpost)))

            assertThat(
                mockMvc.get()
                    .uri("/api/journalpost/{journalpostId}/{dokumentId}", journalpostId, ukjentDokumentId)
            )
                .hasStatus(HttpStatus.NOT_FOUND)
        }
    }

    @Nested
    inner class HentMetaData {

        @Test
        fun `returnerer 200 OK med journalpost-metadata når journalpost finnes`() {
            val journalpost = DokarkivTestdata.journalPost()
            val journalpostId = journalpost.journalpostId

            whenever(safGraphqlClient.hentJournalpost(any()))
                .thenReturn(HentJournalpostGraphqlResponse(data = HentJournalpostData(journalpost)))

            assertThat(
                mockMvc.get()
                    .uri("/api/journalpost/{journalpostId}/metadata", journalpostId)
            )
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPath("$.journalpostId")
        }

        @Test
        fun `returnerer 200 OK når journalpost ikke finnes i arkivet`() {
            val journalpostId = EksternJournalpostId(faker.numerify("########"))

            whenever(safGraphqlClient.hentJournalpost(any()))
                .thenReturn(HentJournalpostGraphqlResponse(data = null))

            assertThat(
                mockMvc.get()
                    .uri("/api/journalpost/{journalpostId}/metadata", journalpostId)
            )
                .hasStatus(HttpStatus.OK)
        }
    }

    @Nested
    inner class FinnJournalposterForSak {

        @Test
        fun `returnerer journalposter for sak uten inkluderAndreSaker`() {
            val sak = SakTestData.lagreNySak(sakRepository)
            val journalposter = listOf(DokarkivTestdata.journalPost(), DokarkivTestdata.journalPost())

            whenever(safGraphqlClient.dokumentoversiktFagsak(any(), any(), any()))
                .thenReturn(DokumentoversiktFagsakGraphqlResponse(
                    data = DokumentoversiktFagsakData(DokumentoversiktResult(journalposter))
                ))

            assertThat(
                mockMvc.get()
                    .uri("/api/journalpost/sak/{saksnummer}", sak.saksnummer)
            )
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPath("$[0].journalpostId")

            verify(safGraphqlClient).dokumentoversiktFagsak(eq(sak.saksnummer), any(), eq(APP_NAVN))
            verify(safGraphqlClient, never()).dokumentoversiktBruker(any(), any())
        }

        @Test
        fun `returnerer tom liste når ingen journalposter finnes for sak`() {
            val sak = SakTestData.lagreNySak(sakRepository)

            whenever(safGraphqlClient.dokumentoversiktFagsak(any(), any(), any()))
                .thenReturn(DokumentoversiktFagsakGraphqlResponse(
                    data = DokumentoversiktFagsakData(DokumentoversiktResult(emptyList()))
                ))

            assertThat(
                mockMvc.get()
                    .uri("/api/journalpost/sak/{saksnummer}", sak.saksnummer)
            )
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .isEqualTo("[]")
        }

        @Test
        fun `inkluderAndreSaker=true henter journalposter for bruker på tvers av saker`() {
            val sak = SakTestData.lagreNySak(sakRepository)
            val journalposter = listOf(DokarkivTestdata.journalPost(), DokarkivTestdata.journalPost())

            whenever(safGraphqlClient.dokumentoversiktBruker(any(), any()))
                .thenReturn(DokumentoversiktBrukerGraphqlResponse(
                    data = DokumentoversiktBrukerData(DokumentoversiktResult(journalposter))
                ))

            assertThat(
                mockMvc.get()
                    .uri("/api/journalpost/sak/{saksnummer}?inkluderAndreSaker=true", sak.saksnummer)
            )
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPath("$[0].journalpostId")

            verify(safGraphqlClient).dokumentoversiktBruker(eq(sak.fnr), eq(listOf(sak.type.tema)))
            verify(safGraphqlClient, never()).dokumentoversiktFagsak(any(), any(), any())
        }
    }

    @Nested
    @WithMockUser(authorities = [])
    inner class Tilgangskontroll {

        @Test
        fun `returnerer 403 uten READ-tilgang`() {
            val journalpostId = EksternJournalpostId(faker.numerify("########"))

            assertThat(
                mockMvc.get()
                    .uri("/api/journalpost/{journalpostId}/metadata", journalpostId)
            )
                .hasStatus(HttpStatus.FORBIDDEN)
        }
    }
}
