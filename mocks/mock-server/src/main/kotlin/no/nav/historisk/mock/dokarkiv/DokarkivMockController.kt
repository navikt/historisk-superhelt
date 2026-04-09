package no.nav.historisk.mock.dokarkiv

import no.nav.common.types.EksternJournalpostId
import no.nav.dokarkiv.AvsenderMottakerIdType
import no.nav.dokarkiv.BrukerIdType
import no.nav.dokarkiv.DokarkivClient
import no.nav.dokarkiv.EksternDokumentInfoId
import no.nav.dokarkiv.JournalpostRequest
import no.nav.dokarkiv.JournalpostResponse
import no.nav.dokarkiv.OppdaterJournalpostRequest
import no.nav.saf.graphql.JournalStatus
import no.nav.saf.graphql.JournalpostAvsenderMottaker
import no.nav.saf.graphql.JournalpostBruker
import no.nav.saf.graphql.JournalpostDokumentInfo
import no.nav.saf.graphql.JournalpostSak
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

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
                    sak = req.sak.let {
                        JournalpostSak(
                            fagsakId = it.fagsakId.value,
                            fagsaksystem = it.fagsaksystem,
                        )
                    },
                    bruker =
                        req.bruker.let {
                            JournalpostBruker(
                                id = it.id,
                                type = BrukerIdType.FNR,
                            )
                        },
                    dokumenter = req.dokumenter.map {
                        JournalpostDokumentInfo(
                            tittel = it.tittel,
                            dokumentInfoId = EksternDokumentInfoId(UUID.randomUUID().toString()),
                            dokumentvarianter = emptyList(),
                        )
                    },
                )
        val pdf = req.dokumenter.firstOrNull()?.dokumentvarianter?.firstOrNull()?.fysiskDokument
        repository.lagre(id, journalpost, pdf)

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
                    JournalpostBruker(
                        id = req.bruker.id,
                        type = BrukerIdType.FNR,
                    ),
                sak = JournalpostSak(
                    fagsakId = req.sak.fagsakId.value,
                    fagsaksystem = req.sak.fagsaksystem,
                ),
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


        repository.oppdater(journalpostId, oppdatert)

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
        repository.oppdater(journalpostId, oppdatert)
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

