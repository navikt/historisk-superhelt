package no.nav.dokarkiv

import no.nav.common.types.FolkeregisterIdent
import no.nav.common.types.Saksnummer
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient

class DokarkivClient(
    private val restClient: RestClient,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun opprett(
        req: JournalpostRequest,
        forsokFerdigstill: Boolean,
    ): JournalpostResponse =
        restClient.post()
            .uri("/rest/journalpostapi/v1/journalpost?forsoekFerdigstill=$forsokFerdigstill")
            .body(req)
            .retrieve()
            .onStatus({ it.value() == 409 }) { _, _ ->
                logger.info("Journalpost med ref {} i sak {} er allerede opprettet. Ignorerer", req.eksternReferanseId, req.sak.fagsakId)
            }
            .body(JournalpostResponse::class.java)!!


    fun ferdigstill(
        journalPostId: EksternJournalpostId,
        journalfoerendeEnhet: String,
    ) {
        val req = FerdigstillJournalpostRequest(journalfoerendeEnhet)
        restClient.patch()
            .uri("/rest/journalpostapi/v1/journalpost/{journalPostId}/ferdigstill", journalPostId)
            .body(req)
            .retrieve()
            .toBodilessEntity()
    }

    /** Fjerner alle logiske vedlegg fra dokumentet */
    fun setLogiskeVedlegg(
        dokumentInfoId: EksternDokumentInfoId,
        titler: List<String>,
    ) {
        val req = BulkOppdaterLogiskVedleggRequest(titler)
        try {
            restClient.put()
                .uri("/rest/journalpostapi/v1/dokumentInfo/{dokumentInfoId}/logiskVedlegg", dokumentInfoId)
                .body(req)
                .retrieve()
                .toBodilessEntity()
        } catch (e: HttpClientErrorException) {
            if (e.statusCode == HttpStatus.NOT_FOUND) {
                logger.info(
                    "Finner ikke dokumentInfoId=$dokumentInfoId for å fjerne logiske vedlegg. Ignoreres siden det da ikke er noe å fjerne.",
                )
            } else {
                throw e
            }
        }
    }

    data class FerdigstillJournalpostRequest(
        val journalfoerendeEnhet: String,
    )

    /** Bruker og tema må settes selv om det er satt fra før... */
    fun oppdaterJournalpost(
        journalPostId: EksternJournalpostId,
        fagsaksnummer: Saksnummer,
        tittel: String,
        bruker: FolkeregisterIdent,
        avsender: FolkeregisterIdent,
        tema: EksternFellesKodeverkTema = EksternFellesKodeverkTema.HEL,
        dokumenter: List<DokumentMedTittel>? = null,
    ) {
        val req =
            OppdaterJournalpostRequest(
                sak =
                    DokArkivSak(
                        fagsakId = fagsaksnummer,
                    ),
                tittel = tittel,
                bruker =
                    DokarkivBruker(
                        id = bruker.value,
                        idType = BrukerIdType.FNR,
                    ),
                avsenderMottaker =
                    AvsenderMottaker(
                        id = avsender.value,
                        idType = AvsenderMottakerIdType.FNR,
                    ),
                dokumenter = dokumenter,
                tema = tema,
            )
        restClient.put()
            .uri("/rest/journalpostapi/v1/journalpost/{journalpostId}", journalPostId)
            .body(req)
            .retrieve()
            .toBodilessEntity()
    }

    data class BulkOppdaterLogiskVedleggRequest(
        val titler: List<String>,
    )

}
