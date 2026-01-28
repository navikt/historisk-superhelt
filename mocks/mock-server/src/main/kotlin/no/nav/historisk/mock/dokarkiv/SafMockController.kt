package no.nav.historisk.mock.dokarkiv

import no.nav.common.types.EksternJournalpostId
import no.nav.historisk.mock.pdl.GraphqlQuery
import no.nav.saf.graphql.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("saf-mock")
class SafMockController(
    private val repository: DokarkivTestRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)


    @RequestMapping(value = ["/graphql"], method = [RequestMethod.OPTIONS])
    fun graphqlOptions() {
    }

    @GetMapping("/rest/hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}")
    fun lastNedDokument(
        @PathVariable journalpostId: String,
        @PathVariable dokumentInfoId: String,
        @PathVariable variantFormat: String,
    ): ResponseEntity<ByteArray> {
        val body = pdfdoc
        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfdoc.size.toLong())
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"mock.pdf\"")
            .body(body)
    }

    @PostMapping(value = ["/graphql"], produces = ["application/json"])
    fun graphql(
        @RequestBody body: GraphqlQuery<Variables>,
    ): ResponseEntity<Any> {
        val query = body.query
        if (query.contains("dokumentoversiktFagsak")) {
            val fagsakId = body.variables.fagsakId ?: throw IllegalArgumentException("fagsakId må være satt")
            logger.debug("søker etter journalposter for fagsak: {}", fagsakId)
            val data = repository.finnJournalposterForSak(fagsakId)
            return ResponseEntity.ok(
                DokumentoversiktGraphqlResponse(
                    data = DokumentoversiktData(
                        dokumentoversiktFagsak = DokumentoversiktFagsakResult(
                            data
                        )
                    )
                )
            )
        }
        if (query.contains("hentJournalpost")) {
            val journalpostId =
                body.variables.journalpostId ?: throw IllegalArgumentException("journalpostId må være satt")
            logger.debug("søker etter journalpost: {}", journalpostId)
            val data = repository.findOrCreate(journalpostId)
            return ResponseEntity.ok(HentJournalpostGraphqlResponse(data = HentJournalpostData(data)))
        }
        throw IllegalArgumentException("Ukjent query")
    }


    data class Variables(
        val journalpostId: EksternJournalpostId?,
        val fagsakId: String?
    )

}
