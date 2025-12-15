package no.nav.dokarkiv

import no.nav.common.types.Fnr
import no.nav.common.types.Saksnummer
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.patchForObject
import org.springframework.web.client.postForObject

class DokarkivClient(
    private val restTemplate: RestTemplate,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun opprett(
        req: JournalpostRequest,
        forsokFerdigstill: Boolean,
    ): JournalpostResponse =
        restTemplate.postForObject<JournalpostResponse>(
            "/rest/journalpostapi/v1/journalpost?forsoekFerdigstill=$forsokFerdigstill",
            req,
        )

    fun ferdigstill(
        journalPostId: EksternJournalpostId,
        journalfoerendeEnhet: String,
    ) {
        val req = FerdigstillJournalpostRequest(journalfoerendeEnhet)
        restTemplate.patchForObject<String>(
            "/rest/journalpostapi/v1/journalpost/{journalPostId}/ferdigstill",
            req,
            journalPostId,
        )
    }

    /** Fjerner alle logiske vedlegg fra dokumentet */
    fun setLogiskeVedlegg(
        dokumentInfoId: EksternDokumentInfoId,
        titler: List<String>,
    ) {
        val req = BulkOppdaterLogiskVedleggRequest(titler)
        try {
            restTemplate.put(
                "/rest/journalpostapi/v1/dokumentInfo/{dokumentInfoId}/logiskVedlegg",
                req,
                dokumentInfoId,
            )
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
        bruker: Fnr,
        avsender: Fnr,
        tema: EksternFellesKodeverkTema = EksternFellesKodeverkTema.HEL,
        dokumenter: List<DokumentMedTittel>? = null,
    ) {
        val req =
            OppdaterJournalpostRequest(
                sak =
                    Sak(
                        fagsakId = fagsaksnummer,
                    ),
                tittel = tittel,
                bruker =
                    Bruker(
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
        restTemplate.put(
            "/rest/journalpostapi/v1/journalpost/{journalpostId}",
            req,
            journalPostId,
        )
    }

    data class BulkOppdaterLogiskVedleggRequest(
        val titler: List<String>,
    )

}
