package no.nav.saf.rest

import no.nav.dokarkiv.EksternDokumentInfoId
import no.nav.dokarkiv.EksternJournalpostId
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

class SafRestClient(
    private val restClient: RestClient,
) {

    fun hentDokument(
        journalpostId: EksternJournalpostId,
        dokumentInfoId: EksternDokumentInfoId,
    ): DokumentResponse {
        val responseEntity =
            restClient
                .get()
                .uri(
                    "/rest/hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}",
                    journalpostId.value,
                    dokumentInfoId.value,
                    VariantFormat.ARKIV.name,
                ).retrieve()
                .toEntity(ByteArray::class.java)

        val headers = responseEntity.headers
        val contentType = headers.contentType ?: MediaType.APPLICATION_OCTET_STREAM
        val fileName = headers.contentDisposition.filename
        val contentLength = headers.contentLength
        val data = responseEntity.body ?: ByteArray(0)

        return DokumentResponse(data, contentType, fileName, contentLength)
    }

    enum class VariantFormat {
        ARKIV,
    }
}
