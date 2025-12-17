package no.nav.historisk.superhelt.brev

import no.nav.historisk.superhelt.brev.pdfgen.PdfgenService
import no.nav.historisk.superhelt.dokarkiv.DokarkivService
import no.nav.historisk.superhelt.endringslogg.EndringsloggService
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.sak.Sak
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BrevSendingService(
    private val brevRepository: BrevRepository,
    private val pdfgenService: PdfgenService,
    private val dokarkivService: DokarkivService,
    private val endringsloggService: EndringsloggService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun sendBrev(sak: Sak, brev: Brev) {
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
        var oppdatertBrev = brevRepository.oppdater(
            uuid = brevId,
            oppdatering = BrevOppdatering(status = BrevStatus.KLAR_TIL_SENDING)
        )
        // Sett brev til klar til sending
        oppdatertBrev = arkiverBrev(brev = oppdatertBrev, sak = sak)

        dokarkivService.distribuerBrev(sak = sak, brev = oppdatertBrev)

        oppdatertBrev=brevRepository.oppdater(uuid = brevId, oppdatering = BrevOppdatering(status = BrevStatus.SENDT))
        endringsloggService.logChange(
            saksnummer = sak.saksnummer,
            endringsType = EndringsloggType.SENDT_BREV,
            endring = "Brev ${oppdatertBrev.tittel} sendt til ${brev.mottakerType.name.lowercase()}",
        )
    }

    private fun arkiverBrev(brev: Brev, sak: Sak): Brev {

        val brevId = brev.uuid
        if (brev.journalpostId != null) {
            log.info("Brev med id $brevId er allerede arkivert med journalpostId ${brev.journalpostId}, hopper over arkivering")
            return brev
        }
        val pdf = pdfgenService.genererPdf(sak, brev)
        val arkivResponse = dokarkivService.arkiver(sak, brev, pdf)

        if (!arkivResponse.journalpostferdigstilt) {
            log.error("Brev med id $brevId i sak ${sak.saksnummer} og journalpostId ${arkivResponse.journalpostId} ble ikke ferdigstilt i dokarkiv. Dette må følges opp")
        }
        return brevRepository.oppdater(
            brevId,
            BrevOppdatering(
                journalpostId = arkivResponse.journalpostId,
            )
        )
    }
}