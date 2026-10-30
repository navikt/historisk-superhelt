package no.nav.historisk.superhelt.brev

import no.nav.historisk.superhelt.brev.pdfgen.PdfgenService
import no.nav.historisk.superhelt.dokarkiv.DokarkivService
import no.nav.historisk.superhelt.dokarkiv.DokdistService
import no.nav.historisk.superhelt.endringslogg.EndringsloggService
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.historisk.superhelt.sak.Sak
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.time.Instant

@Service
class BrevService(
    private val brevRepository: BrevRepository,
    private val endringsloggService: EndringsloggService,
    private val pdfgenService: PdfgenService,
    private val dokarkivService: DokarkivService,
    private val dokdistService: DokdistService,
    private val transactionTemplate: TransactionTemplate,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun hentBrev(sak: Sak, brevId: BrevId): Brev {
        val brev = brevRepository.getByUUid(brevId)
        if (brev.saksnummer != sak.saksnummer) {
            throw IkkeFunnetException("Brev ikke funnet")
        }
        return brev
    }

    internal fun klargjorForSending(sak: Sak, brevId: BrevId): Brev {
        val brev =hentBrev(sak, brevId)
        if (brev.status != BrevStatus.UTKAST) return brev

        BrevValidator(brev)
            .checkBrev()
            .checkKanSendes(sak)
            .validate()

        return brevRepository.oppdaterStatus(brevId, BrevStatus.KLAR_TIL_SENDING, feil = false)
    }


    @Transactional(propagation = Propagation.NEVER)
    internal fun arkiver(sak: Sak, brevId: BrevId): Brev {
        var brev = hentBrev(sak, brevId)
        if (brev.status != BrevStatus.KLAR_TIL_SENDING) return brev

        if (brev.journalpostId == null) {
            val pdf = pdfgenService.genererPdf(sak, brev)
            val respons = dokarkivService.arkiver(sak, brev, pdf)

            brev = brevRepository.oppdaterStatus(
                brevId = brevId,
                journalpostId = respons.journalpostId,
                feil = false
            )
            logger.info(
                "Brev {} i sak {} arkivert med journalpostId {} ferdigstilt {}",
                brevId,
                sak.saksnummer,
                respons.journalpostId,
                respons.journalpostferdigstilt
            )
            // Hvis journalposten ikke ble ferdigstilt, prøv å ferdigstille den på nytt
            if (!respons.journalpostferdigstilt) {
                dokarkivService.ferdigstill(sak, brev)
            }

        } else {
            dokarkivService.ferdigstill(sak, brev)
        }
        return brevRepository.oppdaterStatus(brevId, BrevStatus.ARKIVERT)
    }


    @Transactional(propagation = Propagation.NEVER)
    fun distribuer(sak: Sak, brevId: BrevId): Brev {
        val brev = hentBrev(sak, brevId)
        if (brev.status != BrevStatus.ARKIVERT) return brev

        dokdistService.distribuer(brev)
        return transactionTemplate.execute {
            endringsloggService.logChange(
                saksnummer = sak.saksnummer,
                endringsType = EndringsloggType.SENDT_BREV,
                endring = "Brev sendt til ${brev.mottakerType.name.lowercase()}",
                beskrivelse = "Brev er arkivert og sendt til ${brev.mottakerType.name.lowercase()}",
            )
            brevRepository.oppdaterStatus(brevId, BrevStatus.SENDT, feil = false)
        }
    }


    @Transactional
    internal fun genererNyttBrev(
        sak: Sak,
        type: BrevType,
        mottaker: BrevMottaker): Brev {
        val brevTekstGenerator = BrevTekstGenerator(sak)

        val brev = Brev(
            uuid = BrevId.random(),
            tittel = brevTekstGenerator.generateTittel(type, mottaker),
            innhold = brevTekstGenerator.generateInnhold(type, mottaker),
            type = type,
            mottakerType = mottaker,
            saksnummer = sak.saksnummer,
            opprettetTidspunkt = Instant.now()
        )

        logger.info("Genererer nytt brev med id ${brev.uuid} for sak ${sak.saksnummer} av type ${brev.type} og mottaker ${brev.mottakerType}")

        return brevRepository.opprettBrev(brev)
    }
}
