package no.nav.historisk.superhelt.dokarkiv

import no.nav.common.consts.APP_NAVN
import no.nav.common.types.EksternJournalpostId
import no.nav.dokdist.DistribuerJournalpostRequest
import no.nav.dokdist.DokdistClient
import no.nav.dokdist.DokdistRespons
import no.nav.historisk.superhelt.brev.BrevTestdata
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever


class DokdistServiceTest {
    private var dokdistClient = Mockito.mock(DokdistClient::class.java)
    private var dokdistService: DokdistService = DokdistService(dokdistClient)

    @BeforeEach
    fun reset() {
        Mockito.clearInvocations(dokdistClient)
    }

    @Test
    fun `distribuer skal kalle dokdistClient med riktige parametere for vedtaksbrev`() {
        val journalpostId = EksternJournalpostId("JP123")
        val brev = BrevTestdata.vedtaksbrevBruker().copy(journalpostId = journalpostId)
        val expectedResponse = DokdistRespons(bestillingsId = "BEST123", sendtOk = true)

        whenever(dokdistClient.distribuerJournalpost(any())).thenReturn(expectedResponse)

        val result = dokdistService.distribuer(brev)

        assertEquals(expectedResponse, result)

        val captor = argumentCaptor<DistribuerJournalpostRequest>()
        verify(dokdistClient).distribuerJournalpost(captor.capture())

        val captured = captor.firstValue
        assertEquals(journalpostId, captured.journalpostId)
        assertEquals(APP_NAVN, captured.bestillendeFagsystem)
        assertEquals(DistribuerJournalpostRequest.Distribusjonstype.VEDTAK, captured.distribusjonstype)
        assertEquals(APP_NAVN, captured.dokumentProdApp)
        assertEquals(DistribuerJournalpostRequest.Distribusjonstidspunkt.UMIDDELBART, captured.distribusjonstidspunkt)
    }

    @Test
    fun `distribuer skal kaste exception når journalpostId mangler`() {
        val brev = BrevTestdata.vedtaksbrevBruker()

        val exception = assertThrows(IllegalStateException::class.java) {
            dokdistService.distribuer(brev)
        }

        assertTrue(exception.message!!.contains("Kan ikke distribuere brev uten journalpostId"))
        verify(dokdistClient, never()).distribuerJournalpost(any())
    }

    @Test
    fun `distribuer skal bruke distribusjonstype ANNET for ikke-vedtaksbrev`() {
        val journalpostId = EksternJournalpostId("JP456")
        val brev = BrevTestdata.fritekstbrevBruker().copy(journalpostId = journalpostId)

        whenever(dokdistClient.distribuerJournalpost(any()))
            .thenReturn(DokdistRespons(bestillingsId = "BEST456", sendtOk = true))

        dokdistService.distribuer(brev)

        val captor = argumentCaptor<DistribuerJournalpostRequest>()
        verify(dokdistClient).distribuerJournalpost(captor.capture())
        assertEquals(DistribuerJournalpostRequest.Distribusjonstype.ANNET, captor.firstValue.distribusjonstype)
    }
}
