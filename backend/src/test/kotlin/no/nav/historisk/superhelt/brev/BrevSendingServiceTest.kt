package no.nav.historisk.superhelt.brev

import no.nav.common.types.EksternJournalpostId
import no.nav.dokarkiv.JournalpostResponse
import no.nav.historisk.superhelt.brev.pdfgen.PdfgenService
import no.nav.historisk.superhelt.dokarkiv.DokarkivService
import no.nav.historisk.superhelt.dokarkiv.DokdistService
import no.nav.historisk.superhelt.endringslogg.EndringsloggService
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.WithSaksbehandler
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientException

@MockedSpringBootTest
@WithSaksbehandler
class BrevSendingServiceTest {

    @Autowired
    private lateinit var brevSendingService: BrevSendingService

    @Autowired
    private lateinit var brevRepository: BrevRepository

    @Autowired
    private lateinit var sakRepository: SakRepository

    @Autowired
    private lateinit var endringsloggService: EndringsloggService

    @MockitoBean
    private lateinit var pdfgenService: PdfgenService

    @MockitoBean
    private lateinit var dokarkivService: DokarkivService

    @MockitoBean
    private lateinit var dokdistService: DokdistService

    private fun mockPdfgenSuccess(pdf: ByteArray = "test-pdf".toByteArray()) {
        whenever(pdfgenService.genererPdf(any(), any())).thenReturn(pdf)
    }

    private fun mockDokarkivSuccess(
        journalpostId: String = "123456789",
        journalpostferdigstilt: Boolean = true,
    ) {
        whenever(dokarkivService.arkiver(any(), any(), any())).thenReturn(
            JournalpostResponse(
                journalpostId = EksternJournalpostId(journalpostId),
                journalpostferdigstilt = journalpostferdigstilt,
                dokumenter = emptyList()
            )
        )
    }

    private fun mockDokdistManglerAdresse() {
        whenever(dokdistService.distribuer(any()))
            .thenThrow(HttpClientErrorException(HttpStatus.GONE))
    }

    private fun mockPdfgenFailure(exception: Exception = RestClientException("PDF generation failed")) {
        whenever(pdfgenService.genererPdf(any(), any())).thenThrow(exception)
    }

    private fun mockDokarkivFailure(exception: Exception = RestClientException("Arkivering failed")) {
        whenever(dokarkivService.arkiver(any(), any(), any())).thenThrow(exception)
    }

    private fun mockDokdistFailure(exception: Exception = RestClientException("Distribusjon failed")) {
        whenever(dokdistService.distribuer(any())).thenThrow(exception)
    }

    private fun lagreSakMedBrev(
        status: BrevStatus = BrevStatus.UTKAST,
        tittel: String = "Test tittel",
        innhold: String = "Test innhold",
        mottakerType: BrevMottaker = BrevMottaker.BRUKER,
        journalpostId: EksternJournalpostId? = null
    ): Pair<Sak, Brev> {
        val sak = SakTestData.lagreSak(sakRepository, SakTestData.sakUtenUtbetaling())
        val brev = BrevTestdata.lagreBrev(
            brevRepository = brevRepository,
            saksnummer = sak.saksnummer,
            brev = BrevTestdata.fritekstbrevBruker().copy(
                status = status,
                tittel = tittel,
                innhold = innhold,
                mottakerType = mottakerType,
                journalpostId = journalpostId
            )
        )

        return Pair(sak, brevRepository.getByUUid(brev.uuid))
    }

    @Test
    fun `skal sende brev når status er UTKAST`() {
        mockPdfgenSuccess()
        mockDokarkivSuccess()

        val (sak, brev) = lagreSakMedBrev(status = BrevStatus.UTKAST)

        brevSendingService.sendBrev(sak, brev)

        val oppdatertBrev = brevRepository.getByUUid(brev.uuid)
        assertThat(oppdatertBrev.status).isEqualTo(BrevStatus.SENDT)
        assertThat(oppdatertBrev.journalpostId).isNotNull()
        verify(pdfgenService).genererPdf(any(), any())
        verify(dokarkivService).arkiver(any(), any(), any())
        verify(dokdistService).distribuer(any())
    }

    @Test
    fun `skal ikke gjøre noe når brev allerede er sendt`() {

        val (sak, brev) = lagreSakMedBrev(status = BrevStatus.SENDT)

        brevSendingService.sendBrev(sak, brev)

        verify(pdfgenService, never()).genererPdf(any(), any())
        verify(dokarkivService, never()).arkiver(any(), any(), any())
        verify(dokdistService, never()).distribuer(any())
    }

    @Test
    fun `skal avvise sending og markere feil når brev mangler tittel`() {
        val (sak, brev) = lagreSakMedBrev(status = BrevStatus.UTKAST, tittel = "")

        brevSendingService.sendBrev(sak, brev)

        val lagretBrev = brevRepository.getByUUid(brev.uuid)
        assertThat(lagretBrev.status).isEqualTo(BrevStatus.UTKAST)
        assertThat(lagretBrev.feil).isTrue()
        verify(pdfgenService, never()).genererPdf(any(), any())
        verify(dokarkivService, never()).arkiver(any(), any(), any())
        verify(dokdistService, never()).distribuer(any())
    }

    @Test
    fun `skal avvise sending og markere feil når brev mangler innhold`() {
        val (sak, brev) = lagreSakMedBrev(status = BrevStatus.UTKAST, innhold = "")

        brevSendingService.sendBrev(sak, brev)

        val lagretBrev = brevRepository.getByUUid(brev.uuid)
        assertThat(lagretBrev.status).isEqualTo(BrevStatus.UTKAST)
        assertThat(lagretBrev.feil).isTrue()
        verify(pdfgenService, never()).genererPdf(any(), any())
        verify(dokarkivService, never()).arkiver(any(), any(), any())
        verify(dokdistService, never()).distribuer(any())
    }

    @Test
    fun `skal arkivere brev før distribusjon`() {
        mockPdfgenSuccess()
        mockDokarkivSuccess()

        val (sak, brev) = lagreSakMedBrev(status = BrevStatus.UTKAST)

        brevSendingService.sendBrev(sak, brev)

        val inOrder = inOrder(pdfgenService, dokarkivService, dokdistService)
        inOrder.verify(pdfgenService).genererPdf(any(), any())
        inOrder.verify(dokarkivService).arkiver(any(), any(), any())
        inOrder.verify(dokdistService).distribuer(any())
    }

    @Test
    fun `skal ikke arkivere på nytt hvis brev allerede har journalpostId`() {

        val eksisterendeJournalpostId = EksternJournalpostId("987654321")
        val (sak, brev) = lagreSakMedBrev(
            status = BrevStatus.KLAR_TIL_SENDING,
            journalpostId = eksisterendeJournalpostId
        )

        brevSendingService.sendBrev(sak, brev)

        verify(pdfgenService, never()).genererPdf(any(), any())
        verify(dokarkivService, never()).arkiver(any(), any(), any())
        verify(dokdistService).distribuer(any())
    }

    @Test
    fun `skal sette status til KLAR_TIL_SENDING før arkivering`() {
        mockPdfgenSuccess()
        mockDokarkivSuccess()

        val (sak, brev) = lagreSakMedBrev(status = BrevStatus.UTKAST)

        brevSendingService.sendBrev(sak, brev)

        val oppdatertBrev = brevRepository.getByUUid(brev.uuid)
        assertThat(oppdatertBrev.status).isEqualTo(BrevStatus.SENDT)
    }

    @Test
    fun `skal håndtere feil ved PDF-generering`() {
        mockPdfgenFailure()

        val (sak, brev) = lagreSakMedBrev(status = BrevStatus.UTKAST)

        brevSendingService.sendBrev(sak, brev)

        assertThat(brevRepository.getByUUid(brev.uuid).status).isEqualTo(BrevStatus.KLAR_TIL_SENDING)
        assertThat(brevRepository.getByUUid(brev.uuid).feil).isTrue()
        verify(dokarkivService, never()).arkiver(any(), any(), any())
        verify(dokdistService, never()).distribuer(any())
    }

    @Test
    fun `skal håndtere feil ved arkivering`() {
        mockPdfgenSuccess()
        mockDokarkivFailure()

        val (sak, brev) = lagreSakMedBrev(status = BrevStatus.UTKAST)

        brevSendingService.sendBrev(sak, brev)

        assertThat(brevRepository.getByUUid(brev.uuid).status).isEqualTo(BrevStatus.KLAR_TIL_SENDING)
        assertThat(brevRepository.getByUUid(brev.uuid).feil).isTrue()
        verify(dokdistService, never()).distribuer(any())
    }

    @Test
    fun `skal beholde journalpostreferansen når ferdigstilling feiler`() {
        mockPdfgenSuccess()
        mockDokarkivSuccess(journalpostferdigstilt = false)
        whenever(dokarkivService.ferdigstill(any(), any()))
            .thenThrow(RestClientException("Ferdigstilling feilet"))
        val (sak, brev) = lagreSakMedBrev()

        brevSendingService.sendBrev(sak, brev)

        val lagretBrev = brevRepository.getByUUid(brev.uuid)
        assertThat(lagretBrev.status).isEqualTo(BrevStatus.KLAR_TIL_SENDING)
        assertThat(lagretBrev.journalpostId).isEqualTo(EksternJournalpostId("123456789"))
        assertThat(lagretBrev.feil).isTrue()
        verify(dokarkivService).ferdigstill(
            eq(sak),
            check { assertThat(it.journalpostId).isEqualTo(lagretBrev.journalpostId) },
        )
        verify(dokdistService, never()).distribuer(any())
    }

    @Test
    fun `skal håndtere feil ved distribusjon`() {
        mockPdfgenSuccess()
        mockDokarkivSuccess()
        mockDokdistFailure()

        val (sak, brev) = lagreSakMedBrev(status = BrevStatus.UTKAST)

        brevSendingService.sendBrev(sak, brev)

        val oppdatertBrev = brevRepository.getByUUid(brev.uuid)
        assertThat(oppdatertBrev.journalpostId).isNotNull()
        assertThat(oppdatertBrev.status).isEqualTo(BrevStatus.ARKIVERT)
        assertThat(oppdatertBrev.feil).isTrue()
    }

    @Test
    fun `skal sende riktig PDF til dokarkiv`() {
        val forventetPdf = "test-pdf-innhold".toByteArray()
        mockPdfgenSuccess(forventetPdf)
        mockDokarkivSuccess()

        val (sak, brev) = lagreSakMedBrev(status = BrevStatus.UTKAST)

        brevSendingService.sendBrev(sak, brev)

        verify(dokarkivService).arkiver(
            eq(sak),
            any(),
            eq(forventetPdf)
        )
    }

    @Test
    fun `skal logge endring når brev sendes`() {
        mockPdfgenSuccess()
        mockDokarkivSuccess()

        val (sak, brev) = lagreSakMedBrev(
            status = BrevStatus.UTKAST,
            tittel = "Vedtaksbrev om tilbakebetaling"
        )

        brevSendingService.sendBrev(sak, brev)

        assertThat(endringsloggService.findBySak(sak.saksnummer))
            .extracting<EndringsloggType> { it.type }
            .containsExactly(EndringsloggType.SENDT_BREV)
    }

    @Test
    fun `skal beholde ARKIVERT og kreve manuell oppfølging når brev mangler adresse (410)`() {
        mockPdfgenSuccess()
        mockDokarkivSuccess()
        mockDokdistManglerAdresse()

        val (sak, brev) = lagreSakMedBrev(
            status = BrevStatus.UTKAST,
            tittel = "Vedtaksbrev"
        )

        brevSendingService.sendBrev(sak, brev)

        val oppdatertBrev = brevRepository.getByUUid(brev.uuid)
        assertThat(oppdatertBrev.status).isEqualTo(BrevStatus.ARKIVERT)
        assertThat(oppdatertBrev.feil).isTrue()

        val endringslogg = endringsloggService.findBySak(sak.saksnummer)
        assertThat(endringslogg).isEmpty()
    }

}
