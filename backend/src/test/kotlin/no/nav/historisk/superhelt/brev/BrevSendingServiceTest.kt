package no.nav.historisk.superhelt.brev

import no.nav.dokarkiv.EksternJournalpostId
import no.nav.dokarkiv.JournalpostResponse
import no.nav.dokdist.DistribuerJournalpostResponse
import no.nav.historisk.superhelt.brev.pdfgen.PdfgenService
import no.nav.historisk.superhelt.dokarkiv.DokarkivService
import no.nav.historisk.superhelt.infrastruktur.validation.ValideringException
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.WithSaksbehandler
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean

@MockedSpringBootTest
@WithSaksbehandler
class BrevSendingServiceTest {

    @Autowired
    private lateinit var brevSendingService: BrevSendingService

    @Autowired
    private lateinit var brevRepository: BrevRepository

    @Autowired
    private lateinit var sakRepository: SakRepository

    @MockitoBean
    private lateinit var pdfgenService: PdfgenService

    @MockitoBean
    private lateinit var dokarkivService: DokarkivService



    private fun mockPdfgenSuccess(pdf: ByteArray = "test-pdf".toByteArray()) {
        whenever(pdfgenService.genererPdf(any(), any())).thenReturn(pdf)
    }

    private fun mockDokarkivSuccess(journalpostId: String = "123456789") {
        whenever(dokarkivService.arkiver(any(), any(), any())).thenReturn(
            JournalpostResponse(
                journalpostId = EksternJournalpostId(journalpostId),
                journalpostferdigstilt = true,
                dokumenter = emptyList()
            )
        )
    }

    private fun mockDokdistSuccess(bestillingsId: String = "bestilling-123") {
        whenever(dokarkivService.distribuerBrev(any(), any())).thenReturn(
            DistribuerJournalpostResponse(bestillingsId = bestillingsId)
        )
    }

    private fun mockPdfgenFailure(exception: Exception = RuntimeException("PDF generation failed")) {
        whenever(pdfgenService.genererPdf(any(), any())).thenThrow(exception)
    }

    private fun mockDokarkivFailure(exception: Exception = RuntimeException("Arkivering failed")) {
        whenever(dokarkivService.arkiver(any(), any(), any())).thenThrow(exception)
    }

    private fun mockDokdistFailure(exception: Exception = RuntimeException("Distribusjon failed")) {
        whenever(dokarkivService.distribuerBrev(any(), any())).thenThrow(exception)
    }

    private fun lagreSakMedBrev(
        status: BrevStatus = BrevStatus.NY,
        tittel: String = "Test tittel",
        innhold: String = "Test innhold",
        mottakerType: BrevMottaker = BrevMottaker.BRUKER,
        journalpostId: EksternJournalpostId? = null
    ): Pair<Sak, Brev> {
        val sak = SakTestData.lagreNySak(sakRepository)
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

        return Pair(sak, brev)
    }

    @Test
    fun `skal sende brev når status er NY`() {
        mockPdfgenSuccess()
        mockDokarkivSuccess()
        mockDokdistSuccess()

        val (sak, brev) = lagreSakMedBrev(status = BrevStatus.NY)

        brevSendingService.sendBrev(sak, brev)

        val oppdatertBrev = brevRepository.getByUUid(brev.uuid)
        assertThat(oppdatertBrev.status).isEqualTo(BrevStatus.SENDT)
        assertThat(oppdatertBrev.journalpostId).isNotNull()
        verify(pdfgenService).genererPdf(any(), any())
        verify(dokarkivService).arkiver(any(), any(), any())
        verify(dokarkivService).distribuerBrev(any(), any())
    }

    @Test
    fun `skal ikke gjøre noe når brev allerede er sendt`() {

        val (sak, brev) = lagreSakMedBrev(status = BrevStatus.SENDT)

        brevSendingService.sendBrev(sak, brev)

        verify(pdfgenService, never()).genererPdf(any(), any())
        verify(dokarkivService, never()).arkiver(any(), any(), any())
        verify(dokarkivService, never()).distribuerBrev(any(), any())
    }

    @Test
    fun `skal kaste exception når brev mangler tittel`() {
        val (sak, brev) = lagreSakMedBrev(status = BrevStatus.NY, tittel = "")

        assertThrows<ValideringException> {
            brevSendingService.sendBrev(sak, brev)
        }

        verify(pdfgenService, never()).genererPdf(any(), any())
        verify(dokarkivService, never()).arkiver(any(), any(), any())
    }

    @Test
    fun `skal kaste exception når brev mangler innhold`() {
        val (sak, brev) = lagreSakMedBrev(status = BrevStatus.NY, innhold = "")

        assertThrows<ValideringException> {
            brevSendingService.sendBrev(sak, brev)
        }

        verify(pdfgenService, never()).genererPdf(any(), any())
        verify(dokarkivService, never()).arkiver(any(), any(), any())
    }

    @Test
    fun `skal arkivere brev før distribusjon`() {
        mockPdfgenSuccess()
        mockDokarkivSuccess()
        mockDokdistSuccess()

        val (sak, brev) = lagreSakMedBrev(status = BrevStatus.NY)

        brevSendingService.sendBrev(sak, brev)

        val inOrder = inOrder(pdfgenService, dokarkivService)
        inOrder.verify(pdfgenService).genererPdf(any(), any())
        inOrder.verify(dokarkivService).arkiver(any(), any(), any())
        inOrder.verify(dokarkivService).distribuerBrev(any(), any())
    }

    @Test
    fun `skal ikke arkivere på nytt hvis brev allerede har journalpostId`() {
        mockDokdistSuccess()

        val eksisterendeJournalpostId = EksternJournalpostId("987654321")
        val (sak, brev) = lagreSakMedBrev(
            status = BrevStatus.KLAR_TIL_SENDING,
            journalpostId = eksisterendeJournalpostId
        )

        brevSendingService.sendBrev(sak, brev)

        verify(pdfgenService, never()).genererPdf(any(), any())
        verify(dokarkivService, never()).arkiver(any(), any(), any())
        verify(dokarkivService).distribuerBrev(any(), any())
    }

    @Test
    fun `skal sette status til KLAR_TIL_SENDING før arkivering`() {
        mockPdfgenSuccess()
        mockDokarkivSuccess()
        mockDokdistSuccess()

        val (sak, brev) = lagreSakMedBrev(status = BrevStatus.NY)

        brevSendingService.sendBrev(sak, brev)

        val oppdatertBrev = brevRepository.getByUUid(brev.uuid)
        assertThat(oppdatertBrev.status).isEqualTo(BrevStatus.SENDT)
    }

    @Test
    fun `skal håndtere feil ved PDF-generering`() {
        mockPdfgenFailure()

        val (sak, brev) = lagreSakMedBrev(status = BrevStatus.NY)

        assertThrows<RuntimeException> {
            brevSendingService.sendBrev(sak, brev)
        }

        verify(dokarkivService, never()).arkiver(any(), any(), any())
        verify(dokarkivService, never()).distribuerBrev(any(), any())
    }

    @Test
    fun `skal håndtere feil ved arkivering`() {
        mockPdfgenSuccess()
        mockDokarkivFailure()

        val (sak, brev) = lagreSakMedBrev(status = BrevStatus.NY)

        assertThrows<RuntimeException> {
            brevSendingService.sendBrev(sak, brev)
        }

        verify(dokarkivService, never()).distribuerBrev(any(), any())
    }

    @Test
    fun `skal håndtere feil ved distribusjon`() {
        mockPdfgenSuccess()
        mockDokarkivSuccess()
        mockDokdistFailure()

        val (sak, brev) = lagreSakMedBrev(status = BrevStatus.NY)

        assertThrows<RuntimeException> {
            brevSendingService.sendBrev(sak, brev)
        }

        val oppdatertBrev = brevRepository.getByUUid(brev.uuid)
        assertThat(oppdatertBrev.journalpostId).isNotNull()
        assertThat(oppdatertBrev.status).isNotEqualTo(BrevStatus.SENDT)
    }

    @Test
    fun `skal sende riktig PDF til dokarkiv`() {
        val forventetPdf = "test-pdf-innhold".toByteArray()
        mockPdfgenSuccess(forventetPdf)
        mockDokarkivSuccess()
        mockDokdistSuccess()

        val (sak, brev) = lagreSakMedBrev(status = BrevStatus.NY)

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
        mockDokdistSuccess()

        val (sak, brev) = lagreSakMedBrev(
            status = BrevStatus.NY,
            tittel = "Vedtaksbrev om tilbakebetaling"
        )

        brevSendingService.sendBrev(sak, brev)
    }


}