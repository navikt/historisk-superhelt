package no.nav.historisk.superhelt.brev

import no.nav.historisk.superhelt.brev.pdfgen.PdfgenService
import no.nav.historisk.superhelt.dokarkiv.DokarkivService
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BrevSendingService(
    private val brevRepository: BrevRepository,
    private val brevService: BrevService,
    private val sakRepository: SakRepository,
    private val pdfgenService: PdfgenService,
    private val dokarkivService: DokarkivService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun sendBrev(sak: Sak, brev: BrevUtkast) {
        val brevId = brev.uuid

        BrevValidator(brev)
            .checkBrev()
            .checkKanSendes(sak)
            .validate()

        if (brev.status == BrevStatus.SENDT) {
            log.info("Brev med id $brevId er allerede sendt, kan ikke sende på nytt")
            return
        }

        // sjekk om brev er arkivert
        brevRepository.oppdater(brevId, BrevOppdatering(status = BrevStatus.KLAR_TIL_SENDING))
        // Sett brev til klar til sending
        arkiverBrev(brev, sak, brevId)
        // Todo hva hvis det ikke er fullført i dokarkiv?



        //send til dokdist
        // kvittere ut i brevstatus
        brevRepository.oppdater(brevId, BrevOppdatering(status = BrevStatus.SENDT))

    }

    private fun arkiverBrev(
        brev: BrevUtkast,
        sak: Sak,
        brevId: BrevId) {
        if (brev.journalpostId != null) {
            log.info("Brev med id $brevId er allerede arkivert med journalpostId ${brev.journalpostId}, hopper over arkivering")
            return
        }
        val pdf = pdfgenService.genererPdf(sak, brev)
        val arkivResponse = dokarkivService.arkiver(sak, brev, pdf)
        brevRepository.oppdater(
            brevId,
            BrevOppdatering(
                journalpostId = arkivResponse.journalpostId,
            )
        )
        if (!arkivResponse.journalpostferdigstilt) {
            log.error("Brev med id $brevId i sak ${sak.saksnummer} og journalpostId ${arkivResponse.journalpostId} ble ikke ferdigstilt i dokarkiv. Dette må følges opp")
        }
    }
}