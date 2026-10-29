package no.nav.historisk.superhelt.dokarkiv

import no.nav.common.consts.APP_NAVN
import no.nav.dokdist.DistribuerJournalpostRequest
import no.nav.dokdist.DokdistClient
import no.nav.historisk.superhelt.brev.Brev
import no.nav.historisk.superhelt.brev.BrevType
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class DokdistService(private val dokdistClient: DokdistClient) {
        private val logger = LoggerFactory.getLogger(javaClass)

    @PreAuthorize("hasAuthority('WRITE')")
    fun distribuer(brev: Brev) {
        val journalPostId = brev.journalpostId
            ?: throw IllegalStateException("Kan ikke distribuere brev uten journalpostId. BrevId=${brev.uuid}")

        val respons = dokdistClient.distribuerJournalpost(
            request = DistribuerJournalpostRequest(
                journalpostId = journalPostId,
                bestillendeFagsystem = APP_NAVN,
                distribusjonstype = when (brev.type) {
                    BrevType.VEDTAKSBREV -> DistribuerJournalpostRequest.Distribusjonstype.VEDTAK
                    else -> DistribuerJournalpostRequest.Distribusjonstype.ANNET
                },
                dokumentProdApp = APP_NAVN,
                distribusjonstidspunkt = DistribuerJournalpostRequest.Distribusjonstidspunkt.UMIDDELBART
            )
        )
        logger.info("Brev med er distribuert med journalpostId $journalPostId. DistribusjonId=${respons.bestillingsId}")
    }
}
