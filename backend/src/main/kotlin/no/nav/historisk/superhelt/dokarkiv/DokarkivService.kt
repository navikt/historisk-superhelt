package no.nav.historisk.superhelt.dokarkiv

import no.nav.common.types.EksternFellesKodeverkTema
import no.nav.common.types.Enhetsnummer
import no.nav.dokarkiv.*
import no.nav.dokdist.DistribuerJournalpostRequest
import no.nav.dokdist.DistribuerJournalpostResponse
import no.nav.dokdist.DokdistClient
import no.nav.historisk.superhelt.brev.Brev
import no.nav.historisk.superhelt.brev.BrevType
import no.nav.historisk.superhelt.sak.Sak
import org.springframework.stereotype.Service

@Service
class DokarkivService(
    private val dokarkivClient: DokarkivClient,
    private val dokdistClient: DokdistClient

) {
    fun arkiver(sak: Sak, brev: Brev, pdf: ByteArray): JournalpostResponse {
        val req = JournalpostRequest(
            tittel = brev.tittel!!,
            journalpostType = JournalpostType.UTGAAENDE,
            tema = EksternFellesKodeverkTema.HEL,
            avsenderMottaker = AvsenderMottaker(
                // TODO verge eller samhandler
                id = sak.fnr.value,
                idType = AvsenderMottakerIdType.FNR,
            ),
            eksternReferanseId = brev.uuid.toString(),
            dokumenter = listOf(
                Dokument(
                    tittel = brev.tittel,
                    brevkode = brev.type.name,
                    dokumentvarianter = listOf(
                        DokumentVariant(
                            variantformat = Variantformat.ARKIV,
                            filtype = Filtype.PDF,
                            fysiskDokument = pdf,
                        )
                    )
                )
            ),
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

    fun distribuerBrev(sak: Sak, brev: Brev): DistribuerJournalpostResponse {
        val journalPostId = brev.journalpostId
            ?: throw IllegalStateException("Kan ikke distribuere brev uten journalpostId. BrevId=${brev.uuid}")

        return dokdistClient.distribuerJournalpost(
            request = DistribuerJournalpostRequest(
                journalpostId = journalPostId,
                bestillendeFagsystem = "SUPERHELT",
                distribusjonstype = when (brev.type) {
                    BrevType.VEDTAKSBREV -> DistribuerJournalpostRequest.Distribusjonstype.VEDTAK
                    else -> DistribuerJournalpostRequest.Distribusjonstype.ANNET
                },
                dokumentProdApp = "SUPERHELT",
                distribusjonstidspunkt = DistribuerJournalpostRequest.Distribusjonstidspunkt.UMIDDELBART
            )
        )

    }

}