package no.nav.historisk.superhelt.dokarkiv

import no.nav.common.types.EksternJournalpostId
import no.nav.dokarkiv.EksternDokumentInfoId
import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.saf.graphql.Journalpost
import no.nav.saf.graphql.SafGraphqlClient
import no.nav.saf.rest.DokumentResponse
import no.nav.saf.rest.SafRestClient
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class JournalpostService(
    private val safRestClient: SafRestClient,
    private val safGraphqlClient: SafGraphqlClient,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @PreAuthorize("hasAuthority('READ')")
    fun lastNedEttDokument(
        journalpostId: EksternJournalpostId,
        dokumentId: EksternDokumentInfoId,
    ): DokumentResponse {
        val journalpost =
            hentJournalpost(journalpostId)
                ?: throw IkkeFunnetException("Finner ikke journalpost med id $journalpostId")
        journalpost.dokumenter?.firstOrNull { it.dokumentInfoId == dokumentId }
            ?: throw IkkeFunnetException("Finner ikke dokument med id $dokumentId i journalpost $journalpostId")
        return hentDokument(journalpostId, dokumentId)
    }

    private fun hentDokument(
        journalpostId: EksternJournalpostId,
        dokumentId: EksternDokumentInfoId,
    ): DokumentResponse = safRestClient.hentDokument(journalpostId, dokumentId)

    @PreAuthorize("hasAuthority('READ')")
    fun hentJournalpost(journalpostId: EksternJournalpostId): Journalpost? {
        val journalpost = safGraphqlClient.hentJournalpost(journalpostId).data?.journalpost
        return journalpost
    }


}