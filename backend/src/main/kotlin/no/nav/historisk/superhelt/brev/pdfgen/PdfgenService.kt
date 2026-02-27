package no.nav.historisk.superhelt.brev.pdfgen

import no.nav.historisk.pdfgen.*
import no.nav.historisk.superhelt.brev.Brev
import no.nav.historisk.superhelt.brev.BrevMottaker
import no.nav.historisk.superhelt.brev.BrevType
import no.nav.historisk.superhelt.person.PersonService
import no.nav.historisk.superhelt.sak.Sak
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class PdfgenService(
    private val pdfgenClient: PdfgenClient,
    private val personService: PersonService

) {
    internal fun mapToPdfgenRequest(sak: Sak, brev: Brev): PdfgenRequest {

        val person = personService.hentPerson(sak.fnr)
            ?: throw IllegalStateException("Fant ikke persondata for person i sak ${sak.saksnummer}")

        val beslutterNavn = when (brev.type) {
            BrevType.VEDTAKSBREV -> sak.attestant?.navn ?: "<attestant>"
            else -> sak.attestant?.navn
        }

        return PdfgenRequest(
            behandlingsnummer = "${sak.saksnummer}-${sak.behandlingsnummer}",
            personalia = Personalia(
                ident = sak.fnr,
                fornavn = person.fornavn,
                etternavn = person.etternavn,
            ),
            datoForUtsending = LocalDate.now(),
            saksbehandlerNavn = sak.saksbehandler.navn,
            beslutterNavn = beslutterNavn,
            kontor = "NAV Arbeid og ytelser",
            html = htmlToXhtml(brev.innhold ?: ""),
            brevtype = brev.type.asPdfGenBrevType(),
            mottaker = brev.mottakerType.asPdfgenMottakerType(),
        )
    }

    fun genererHtml(sak: Sak, brev: Brev): ByteArray {
        val pdfgenRequest = mapToPdfgenRequest(sak, brev)
        return pdfgenClient.genererHtml(pdfgenRequest)
    }

    fun genererPdf(sak: Sak, brev: Brev): ByteArray {
        val pdfgenRequest = mapToPdfgenRequest(sak, brev)
        return pdfgenClient.genererPdf(pdfgenRequest)
    }

    private fun BrevType.asPdfGenBrevType(): PdfgenBrevtype {
        return when (this) {
            BrevType.VEDTAKSBREV -> PdfgenBrevtype.VEDTAKSBREV
            else -> PdfgenBrevtype.BREV
        }
    }

    private fun BrevMottaker.asPdfgenMottakerType(): PdfgenMottakerType {
        return when (this) {
            BrevMottaker.BRUKER -> PdfgenMottakerType.BRUKER
            BrevMottaker.SAMHANDLER -> PdfgenMottakerType.SAMHANDLER
        }
    }

}


