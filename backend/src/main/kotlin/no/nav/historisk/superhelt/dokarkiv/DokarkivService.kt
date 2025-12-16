package no.nav.historisk.superhelt.dokarkiv

import no.nav.dokarkiv.*
import no.nav.historisk.superhelt.brev.BrevUtkast
import no.nav.historisk.superhelt.sak.Sak
import no.nav.pdl.SafGraphqlClient
import no.nav.saf.rest.SafRestClient
import org.springframework.stereotype.Service

@Service
class DokarkivService (
    private val dokarkivClient: DokarkivClient,
    private val safGqlClient: SafGraphqlClient,
    private val safRestClient: SafRestClient

    ){
    fun arkiver(sak: Sak, brev: BrevUtkast, pdf: ByteArray): JournalpostResponse {
        val req = JournalpostRequest(
            tittel = brev.tittel!!,
            journalpostType = JournalpostType.UTGAAENDE,
            tema = EksternFellesKodeverkTema.HEL,
            avsenderMottaker = AvsenderMottaker( // TODO verge eller samhandler
                id = sak.fnr.value,
                idType = AvsenderMottakerIdType.FNR,
            ),
            eksternReferanseId = brev.uuid.toString(),
            dokumenter = listOf(Dokument(
                tittel = brev.tittel,
                brevkode = brev.type.name,
                dokumentvarianter = listOf(
                    DokumentVariant(
                        variantformat = Variantformat.ARKIV,
                        filtype = Filtype.PDF,
                        fysiskDokument = pdf,
                    )
                )
            )),
            bruker = DokarkivBruker(
                id = sak.fnr.value,
                idType = BrukerIdType.FNR,
            ),
            kanal = Kanal.NAV_NO,
            sak = DokArkivSak(
                sakstype = Sakstype.FAGSAK,
                fagsakId = sak.saksnummer,
                fagsaksystem = "HELT",
            ),
            journalfoerendeEnhet = Enhetsnummer("4485") // TODO hente fra sak eller saksbehandler
        )
        return dokarkivClient.opprett(req, forsokFerdigstill = true)
    }
}