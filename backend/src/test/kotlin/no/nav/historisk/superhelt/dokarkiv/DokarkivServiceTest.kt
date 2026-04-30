package no.nav.historisk.superhelt.dokarkiv

import no.nav.common.consts.APP_NAVN
import no.nav.common.consts.FellesKodeverkTema
import no.nav.common.types.EksternJournalpostId
import no.nav.common.types.EksternOppgaveId
import no.nav.common.types.Enhetsnummer
import no.nav.common.types.FolkeregisterIdent
import no.nav.common.types.Saksnummer
import no.nav.dokarkiv.AvsenderMottakerIdType
import no.nav.dokarkiv.BrukerIdType
import no.nav.dokarkiv.DokarkivClient
import no.nav.dokarkiv.DokumentMedTittel
import no.nav.dokarkiv.EksternDokumentInfoId
import no.nav.dokarkiv.JournalpostRequest
import no.nav.dokarkiv.JournalpostResponse
import no.nav.dokarkiv.JournalpostType
import no.nav.dokarkiv.Kanal
import no.nav.dokarkiv.Sakstype
import no.nav.dokdist.DistribuerJournalpostRequest
import no.nav.dokdist.DokdistClient
import no.nav.dokdist.DokdistRespons
import no.nav.historisk.superhelt.StonadsType
import no.nav.historisk.superhelt.brev.BrevTestdata
import no.nav.historisk.superhelt.dokarkiv.rest.JournalforNySakRequest
import no.nav.historisk.superhelt.person.PersonService
import no.nav.historisk.superhelt.person.PersonTestData
import no.nav.historisk.superhelt.sak.SakTestData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.never
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.test.context.support.WithMockUser

@ExtendWith(MockitoExtension::class)
class DokarkivServiceTest {

    @Mock
    private lateinit var dokarkivClient: DokarkivClient

    @Mock
    private lateinit var dokdistClient: DokdistClient

    @Mock
    private lateinit var personService: PersonService

    @InjectMocks
    private lateinit var dokarkivService: DokarkivService

    @Test
    @WithMockUser(authorities = ["WRITE"])
    fun `arkiver skal kalle dokarkivClient med riktige parametere`() {
        val sak = SakTestData.sakUtenUtbetaling()
        val brev = BrevTestdata.vedtaksbrevBruker()
        val pdf = "test pdf".toByteArray()

        val expectedResponse = JournalpostResponse(
            journalpostId = EksternJournalpostId("JP123"),
            journalpostferdigstilt = true,
            dokumenter = emptyList()
        )

        whenever(dokarkivClient.opprett(any(), any())).thenReturn(expectedResponse)

        val result = dokarkivService.arkiver(sak, brev, pdf)

        val journalpostRequestCaptor = argumentCaptor<JournalpostRequest>()
        verify(dokarkivClient).opprett(journalpostRequestCaptor.capture(), eq(true))

        val capturedRequest = journalpostRequestCaptor.firstValue
        assertEquals(brev.tittel, capturedRequest.tittel)
        assertEquals(JournalpostType.UTGAAENDE, capturedRequest.journalpostType)
        assertEquals(FellesKodeverkTema.HEL, capturedRequest.tema)
        assertEquals(sak.fnr.value, capturedRequest.avsenderMottaker?.id)
        assertEquals(AvsenderMottakerIdType.FNR, capturedRequest.avsenderMottaker?.idType)
        assertEquals(brev.uuid.toString(), capturedRequest.eksternReferanseId)
        assertEquals(sak.fnr.value, capturedRequest.bruker.id)
        assertEquals(BrukerIdType.FNR, capturedRequest.bruker.idType)
        assertEquals(Kanal.NAV_NO, capturedRequest.kanal)
        assertEquals(sak.saksnummer, capturedRequest.sak.fagsakId)
        assertEquals(Sakstype.FAGSAK, capturedRequest.sak.sakstype)
        assertEquals(APP_NAVN, capturedRequest.sak.fagsaksystem)
        assertEquals(Enhetsnummer("4485"), capturedRequest.journalfoerendeEnhet)
        assertEquals(1, capturedRequest.dokumenter.size)
        assertEquals(brev.tittel, capturedRequest.dokumenter[0].tittel)
        assertEquals(brev.type.name, capturedRequest.dokumenter[0].brevkode)
    }

    @Test
    @WithMockUser(authorities = ["WRITE"])
    fun `arkiver skal bruke verges FNR som mottaker når verge finnes`() {
        val verge = PersonTestData.testPersonMedAdressebeskyttelse
        val bruker = PersonTestData.testPersonMedVerge
        val sak = SakTestData.sakUtenUtbetaling().copy(fnr = bruker.fnr)
        val brev = BrevTestdata.vedtaksbrevBruker()
        val pdf = "test pdf".toByteArray()

        val expectedResponse = JournalpostResponse(
            journalpostId = EksternJournalpostId("JP123"),
            journalpostferdigstilt = true,
            dokumenter = emptyList()
        )

        whenever(dokarkivClient.opprett(any(), any())).thenReturn(expectedResponse)
        whenever(personService.hentVerge(bruker.fnr)).thenReturn(verge)

        dokarkivService.arkiver(sak, brev, pdf)

        val journalpostRequestCaptor = argumentCaptor<JournalpostRequest>()
        verify(dokarkivClient).opprett(journalpostRequestCaptor.capture(), eq(true))

        val capturedRequest = journalpostRequestCaptor.firstValue
        // Verifiser at vergens FNR brukes som mottaker
        assertEquals(verge.fnr.value, capturedRequest.avsenderMottaker?.id)
        assertEquals(AvsenderMottakerIdType.FNR, capturedRequest.avsenderMottaker?.idType)
        // Bruker (ikke vergen) er fortsatt sakseieren
        assertEquals(sak.fnr.value, capturedRequest.bruker.id)
        assertEquals(BrukerIdType.FNR, capturedRequest.bruker.idType)
    }

    @Test
    @WithMockUser(authorities = ["WRITE"])
    fun `arkiver skal bruke brukers FNR når verge mangler`() {
        val sak = SakTestData.sakUtenUtbetaling()
        val brev = BrevTestdata.vedtaksbrevBruker()
        val pdf = "test pdf".toByteArray()

        val expectedResponse = JournalpostResponse(
            journalpostId = EksternJournalpostId("JP123"),
            journalpostferdigstilt = true,
            dokumenter = emptyList()
        )

        whenever(dokarkivClient.opprett(any(), any())).thenReturn(expectedResponse)
        whenever(personService.hentVerge(sak.fnr)).thenReturn(null)

        dokarkivService.arkiver(sak, brev, pdf)

        val journalpostRequestCaptor = argumentCaptor<JournalpostRequest>()
        verify(dokarkivClient).opprett(journalpostRequestCaptor.capture(), eq(true))

        val capturedRequest = journalpostRequestCaptor.firstValue
        assertEquals(sak.fnr.value, capturedRequest.avsenderMottaker?.id)
        assertEquals(AvsenderMottakerIdType.FNR, capturedRequest.avsenderMottaker?.idType)
    }

    @Test
    @WithMockUser(authorities = ["WRITE"])
    fun `distribuerBrev skal kalle dokdistClient med riktige parametere for vedtaksbrev`() {
        val sak = SakTestData.sakUtenUtbetaling()
        val journalpostId = EksternJournalpostId("JP123")
        val brev = BrevTestdata.vedtaksbrevBruker().copy(journalpostId = journalpostId)
        val expectedResponse = DokdistRespons(bestillingsId = "BEST123", sendtOk = true)

        whenever(dokdistClient.distribuerJournalpost(any())).thenReturn(expectedResponse)

        val result = dokarkivService.distribuerBrev(sak, brev)

        assertEquals(expectedResponse, result)

        val distribuerRequestCaptor = argumentCaptor<DistribuerJournalpostRequest>()
        verify(dokdistClient).distribuerJournalpost(distribuerRequestCaptor.capture())

        val capturedRequest = distribuerRequestCaptor.firstValue
        assertEquals(journalpostId, capturedRequest.journalpostId)
        assertEquals("SUPERHELT", capturedRequest.bestillendeFagsystem)
        assertEquals(DistribuerJournalpostRequest.Distribusjonstype.VEDTAK, capturedRequest.distribusjonstype)
        assertEquals("SUPERHELT", capturedRequest.dokumentProdApp)
        assertEquals(
            DistribuerJournalpostRequest.Distribusjonstidspunkt.UMIDDELBART,
            capturedRequest.distribusjonstidspunkt
        )
    }

    @Test
    @WithMockUser(authorities = ["WRITE"])
    fun `distribuerBrev skal kaste exception når journalpostId mangler`() {
        val sak = SakTestData.sakUtenUtbetaling()
        val brev = BrevTestdata.vedtaksbrevBruker()

        val exception = assertThrows(IllegalStateException::class.java) {
            dokarkivService.distribuerBrev(sak, brev)
        }

        assertTrue(exception.message!!.contains("Kan ikke distribuere brev uten journalpostId"))
        verify(dokdistClient, never()).distribuerJournalpost(any())
    }

    @Test
    @WithMockUser(authorities = ["WRITE"])
    fun `journalførIArkivet skal kalle dokarkivClient med riktige parametere`() {
        val journalPostId = EksternJournalpostId("JP123")
        val fagsaksnummer = Saksnummer("SAK123")
        val journalfoerendeEnhet = Enhetsnummer("4485")
        val bruker = FolkeregisterIdent("12345678901")
        val avsender = FolkeregisterIdent("10987654321")

        val request = JournalforNySakRequest(
            bruker = bruker,
            avsender = avsender,
            dokumenter = listOf(
                JournalforDokument(
                    dokumentInfoId = EksternDokumentInfoId("DOK123"),
                    tittel = "Test dokument",
                    logiskeVedlegg = listOf("Vedlegg 1", "Vedlegg 2")
                )
            ),
            stonadsType = StonadsType.REISEUTGIFTER,
            jfrOppgaveId = EksternOppgaveId(123)
        )

        doNothing().`when`(dokarkivClient).oppdaterJournalpost(any(), any(), any(), any(), any(), any(), any())
        doNothing().`when`(dokarkivClient).setLogiskeVedlegg(any(), any())
        doNothing().`when`(dokarkivClient).ferdigstill(any(), any())

        dokarkivService.journalførIArkivet(journalPostId, fagsaksnummer, journalfoerendeEnhet, FellesKodeverkTema.HEL, request)

        val dokumenterCaptor = argumentCaptor<List<DokumentMedTittel>>()
        verify(dokarkivClient).oppdaterJournalpost(
            journalPostId = eq(journalPostId),
            fagsaksnummer = eq(fagsaksnummer),
            tittel = eq("Test dokument"),
            bruker = eq(bruker),
            avsender = eq(avsender),
            tema = eq(FellesKodeverkTema.HEL),
            dokumenter = dokumenterCaptor.capture()
        )

        val capturedDokumenter = dokumenterCaptor.firstValue
        assertEquals(1, capturedDokumenter.size)
        assertEquals("Test dokument", capturedDokumenter[0].tittel)
        assertEquals(EksternDokumentInfoId("DOK123"), capturedDokumenter[0].dokumentInfoId)

        verify(dokarkivClient).setLogiskeVedlegg(
            eq(EksternDokumentInfoId("DOK123")),
            eq(listOf("Vedlegg 1", "Vedlegg 2"))
        )

        verify(dokarkivClient).ferdigstill(
            eq(journalPostId),
            eq(journalfoerendeEnhet.value)
        )
    }

    @Test
    @WithMockUser(authorities = ["WRITE"])
    fun `journalførIArkivet skal håndtere dokumenter uten logiske vedlegg`() {
        val journalPostId = EksternJournalpostId("JP123")
        val fagsaksnummer = Saksnummer("SAK123")
        val journalfoerendeEnhet = Enhetsnummer("4485")
        val bruker = FolkeregisterIdent("12345678901")
        val avsender = FolkeregisterIdent("10987654321")

        val request = JournalforNySakRequest(
            bruker = bruker,
            avsender = avsender,
            dokumenter = listOf(
                JournalforDokument(
                    dokumentInfoId = EksternDokumentInfoId("DOK123"),
                    tittel = "Test dokument",
                    logiskeVedlegg = null
                )
            ),
            stonadsType = StonadsType.REISEUTGIFTER,
            jfrOppgaveId = EksternOppgaveId(123)
        )

        doNothing().`when`(dokarkivClient).oppdaterJournalpost(any(), any(), any(), any(), any(), any(), any())
        doNothing().`when`(dokarkivClient).setLogiskeVedlegg(any(), any())
        doNothing().`when`(dokarkivClient).ferdigstill(any(), any())

        dokarkivService.journalførIArkivet(journalPostId, fagsaksnummer, journalfoerendeEnhet, FellesKodeverkTema.HEL, request)

        verify(dokarkivClient).setLogiskeVedlegg(
            eq(EksternDokumentInfoId("DOK123")),
            eq(emptyList())
        )
    }
}
