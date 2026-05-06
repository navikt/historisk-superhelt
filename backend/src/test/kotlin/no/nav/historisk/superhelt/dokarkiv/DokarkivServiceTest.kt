package no.nav.historisk.superhelt.dokarkiv

import no.nav.common.consts.APP_NAVN
import no.nav.common.consts.FellesKodeverkTema
import no.nav.common.types.EksternJournalpostId
import no.nav.common.types.EksternOppgaveId
import no.nav.common.types.Enhetsnummer
import no.nav.common.types.FolkeregisterIdent
import no.nav.common.types.Saksnummer
import no.nav.dokarkiv.AvsenderMottaker
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
import no.nav.historisk.superhelt.StonadsType
import no.nav.historisk.superhelt.brev.BrevTestdata
import no.nav.historisk.superhelt.dokarkiv.rest.JournalforNySakRequest
import no.nav.historisk.superhelt.sak.SakTestData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.doNothing
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
    private lateinit var avsenderMottakerResolver: AvsenderMottakerResolver

    @InjectMocks
    private lateinit var dokarkivService: DokarkivService

    @Test
    @WithMockUser(authorities = ["WRITE"])
    fun `arkiver skal kalle dokarkivClient med riktige parametere`() {
        val sak = SakTestData.sakUtenUtbetaling()
        val brev = BrevTestdata.vedtaksbrevBruker()
        val pdf = "test pdf".toByteArray()
        val forventetMottaker = AvsenderMottaker(id = sak.fnr.value, idType = AvsenderMottakerIdType.FNR)

        val expectedResponse = JournalpostResponse(
            journalpostId = EksternJournalpostId("JP123"),
            journalpostferdigstilt = true,
            dokumenter = emptyList()
        )

        whenever(avsenderMottakerResolver.resolve(brev.mottakerType, sak)).thenReturn(forventetMottaker)
        whenever(dokarkivClient.opprett(any(), any())).thenReturn(expectedResponse)

        val result = dokarkivService.arkiver(sak, brev, pdf)

        val journalpostRequestCaptor = argumentCaptor<JournalpostRequest>()
        verify(dokarkivClient).opprett(journalpostRequestCaptor.capture(), eq(true))

        val capturedRequest = journalpostRequestCaptor.firstValue
        assertEquals(brev.tittel, capturedRequest.tittel)
        assertEquals(JournalpostType.UTGAAENDE, capturedRequest.journalpostType)
        assertEquals(sak.type.tema, capturedRequest.tema)
        assertEquals(sak.fnr.value, capturedRequest.avsenderMottaker?.id)
        assertEquals(AvsenderMottakerIdType.FNR, capturedRequest.avsenderMottaker?.idType)
        assertEquals(brev.uuid.toString(), capturedRequest.eksternReferanseId)
        assertEquals(sak.fnr.value, capturedRequest.bruker.id)
        assertEquals(BrukerIdType.FNR, capturedRequest.bruker.idType)
        assertEquals(Kanal.NAV_NO, capturedRequest.kanal)
        assertEquals(sak.saksnummer, capturedRequest.sak.fagsakId)
        assertEquals(Sakstype.FAGSAK, capturedRequest.sak.sakstype)
        assertEquals(APP_NAVN, capturedRequest.sak.fagsaksystem)
        assertEquals(sak.type.enhet, capturedRequest.journalfoerendeEnhet)
        assertEquals(1, capturedRequest.dokumenter.size)
        assertEquals(brev.tittel, capturedRequest.dokumenter[0].tittel)
        assertEquals(brev.type.name, capturedRequest.dokumenter[0].brevkode)
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
