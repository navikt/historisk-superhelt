package no.nav.historisk.pdfgen

import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

class PdfgenClient(
    private val restClient: RestClient) {

    private val brevpath: String = "superhelt/brev"

    fun genererPdf(request: PdfgenRequest): ByteArray {
        return callPdfGen(request, "/api/v1/genpdf/${brevpath}")
    }

    fun genererHtml(request: PdfgenRequest): ByteArray {
        return callPdfGen(request, "/api/v1/genhtml/${brevpath}")
    }

    private fun callPdfGen(request: PdfgenRequest, url: String): ByteArray {
        return restClient
            .post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(ByteArray::class.java)
            ?: throw RuntimeException("Pdfgen returnerte tomt innhold")
    }
}

