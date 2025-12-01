package no.nav.historisk.superhelt.brev.pdfgen

import no.nav.historisk.pdfgen.*
import no.nav.historisk.superhelt.brev.BrevUtkast
import no.nav.historisk.superhelt.person.PersonService
import no.nav.historisk.superhelt.sak.Sak
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class PdfgenService(
    private val pdfgenClient: PdfgenClient,
    private val personService: PersonService

) {
    internal fun mapToPdfgenRequest(sak: Sak, brev: BrevUtkast): PdfgenRequest {

        val person= personService.hentPerson(sak.fnr)

        return PdfgenRequest(
            behandlingsnummer = sak.behandlingsnummer,
            personalia = Personalia(
                ident = sak.fnr,
                fornavn = person?.fornavn ?: "XXX",
                etternavn = person?.etternavn?:"YYY"
            ),
            datoForUtsending = LocalDate.now(),
            saksbehandlerNavn = sak.saksbehandler.value,
            beslutterNavn = sak.attestant?.value ?: "attestant",
            kontor = "NAV Arbeid og ytelser",
            html = brev.innhold?:"",
            brevtype = PdfgenBrevtype.VEDTAKSBREV,
            mottaker = PdfgenMottakerType.BRUKER,
        )
    }

    fun hentHtmlBrev(sak: Sak, brev: BrevUtkast): ByteArray {
        val pdfgenRequest = mapToPdfgenRequest(sak, brev)
        return pdfgenClient.genererHtml(pdfgenRequest)
    }
}