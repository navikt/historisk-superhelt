package no.nav.historisk.mock.dokarkiv

import no.nav.dokarkiv.*
import no.nav.saf.graphql.JournalStatus
import no.nav.saf.graphql.JournalpostAvsenderMottaker
import no.nav.saf.graphql.JournalpostBruker
import no.nav.saf.graphql.JournalpostDokumentInfo
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("dokarkiv-mock")
class DokarkivController(
    private val repository: DokarkivTestRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/rest/journalpostapi/v1/journalpost")
    fun opprettJournalpostMock(
        @RequestParam("forsoekFerdigstill") forsokFerdigStill: Boolean = false,
        @RequestBody req: JournalpostRequest,
    ): JournalpostResponse {
        val id = EksternJournalpostId(faker.numerify("########"))
        logger.info("Oppretter journalpost for id {}", id)
        val journalpost =
            generateJournalpost(id)
                .copy(
                    journalstatus = JournalStatus.JOURNALFOERT,
                    tittel = req.tittel,
                    bruker =
                        req.bruker.let {
                            JournalpostBruker(
                                id = it.id,
                                type = BrukerIdType.FNR,
                            )
                        },
                )
        repository.lagre(id, journalpost)

        return JournalpostResponse(
            journalpostId = EksternJournalpostId(id.value),
            journalpostferdigstilt = forsokFerdigStill,
            dokumenter = emptyList()
        )
    }

    @PutMapping("/rest/journalpostapi/v1/journalpost/{journalpostId}")
    fun oppdaterJournalpostMock(
        @PathVariable journalpostId: EksternJournalpostId,
        @RequestBody req: OppdaterJournalpostRequest,
    ): String {
        logger.info("oppdaterer journalpost for id {}", journalpostId)
        val journalpost = repository.findOrCreate(journalpostId)

        val oppdatert =
            journalpost.copy(
                tittel = req.tittel,
                bruker =
                    req.bruker.let {
                        JournalpostBruker(
                            id = it.id,
                            type = BrukerIdType.FNR,
                        )
                    },
                avsenderMottaker =
                    req.avsenderMottaker.let {
                        JournalpostAvsenderMottaker(
                            id = it.id,
                            type = AvsenderMottakerIdType.FNR,
                            navn = it.navn,
                        )
                    },
                dokumenter =
                    req.dokumenter?.map {
                        JournalpostDokumentInfo(
                            tittel = it.tittel,
                            dokumentInfoId = it.dokumentInfoId,
                            dokumentvarianter = emptyList(),
                        )
                    } ?: journalpost.dokumenter,
            )


        repository.lagre(journalpostId, oppdatert)

        return journalpost.journalpostId.value
    }

    @PatchMapping("/rest/journalpostapi/v1/journalpost/{journalpostId}/ferdigstill")
    fun ferdigStillJournalpostMock(
        @PathVariable journalpostId: EksternJournalpostId,
    ): String {
        logger.info("ferdigstiller journalpost for id {}", journalpostId)

        val journalpost = repository.findOrCreate(journalpostId)

        val oppdatert =
            journalpost.copy(
                journalstatus = JournalStatus.JOURNALFOERT,
            )
        repository.lagre(journalpostId, oppdatert)
        return journalpost.journalpostId.value
    }

    @PutMapping("/rest/journalpostapi/v1/dokumentInfo/{dokumentInfoId}/logiskVedlegg")
    fun bulkOppdaterLogiskeVedlegg(
        @PathVariable dokumentInfoId: EksternDokumentInfoId,
        @RequestBody req: DokarkivClient.BulkOppdaterLogiskVedleggRequest,
    ) {
        logger.info("setter logiske vedlegg {} for dokument id {}", req, dokumentInfoId)
    }


}

