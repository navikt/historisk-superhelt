package no.nav.historisk.superhelt.dokarkiv

import no.nav.common.consts.APP_NAVN
import no.nav.common.types.EksternJournalpostId
import no.nav.common.types.Enhetsnummer
import no.nav.common.types.FolkeregisterIdent
import no.nav.common.types.Saksnummer
import no.nav.common.types.defaultEnhetsnummer
import no.nav.dokarkiv.AvsenderMottaker
import no.nav.dokarkiv.AvsenderMottakerIdType
import no.nav.dokarkiv.BrukerIdType
import no.nav.dokarkiv.DokArkivSak
import no.nav.dokarkiv.DokarkivBruker
import no.nav.dokarkiv.DokarkivClient
import no.nav.dokarkiv.Dokument
import no.nav.dokarkiv.DokumentMedTittel
import no.nav.dokarkiv.DokumentVariant
import no.nav.dokarkiv.Filtype
import no.nav.dokarkiv.JournalpostRequest
import no.nav.dokarkiv.JournalpostResponse
import no.nav.dokarkiv.JournalpostType
import no.nav.dokarkiv.Kanal
import no.nav.dokarkiv.Sakstype
import no.nav.dokarkiv.Variantformat
import no.nav.dokdist.DistribuerJournalpostRequest
import no.nav.dokdist.DokdistClient
import no.nav.dokdist.DokdistRespons
import no.nav.historisk.superhelt.brev.Brev
import no.nav.historisk.superhelt.brev.BrevMottaker
import no.nav.historisk.superhelt.brev.BrevType
import no.nav.historisk.superhelt.person.PersonService
import no.nav.historisk.superhelt.sak.Sak
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class DokarkivService(
    private val dokarkivClient: DokarkivClient,
    private val dokdistClient: DokdistClient,
    private val personService: PersonService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun settMottakerFnrSomVergeEllerBruker(fnr: FolkeregisterIdent): FolkeregisterIdent {
        val verge = personService.hentVerge(vergetrengendeFnr = fnr)
        if (verge != null) logger.debug("Fant verge for bruker, setter verge som mottaker.")
        return verge?.fnr ?: fnr
    }

    @PreAuthorize("hasAuthority('WRITE')")
    fun arkiver(sak: Sak, brev: Brev, pdf: ByteArray): JournalpostResponse {

        val req = JournalpostRequest(
            tittel = brev.tittel!!,
            journalpostType = JournalpostType.UTGAAENDE,
            tema = sak.type.tema,
            avsenderMottaker = when (brev.mottakerType) {
                BrevMottaker.BRUKER ->
                    AvsenderMottaker(
                        id = settMottakerFnrSomVergeEllerBruker(sak.fnr).value,
                        idType = AvsenderMottakerIdType.FNR,
                    )

                BrevMottaker.SAMHANDLER -> TODO("Send til samhandler")
            },
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
                fagsaksystem = APP_NAVN,
            ),
            journalfoerendeEnhet = defaultEnhetsnummer
        )
        return dokarkivClient.opprett(req, forsokFerdigstill = true)
    }

    @PreAuthorize("hasAuthority('WRITE')")
    fun distribuerBrev(sak: Sak, brev: Brev): DokdistRespons {
        val journalPostId = brev.journalpostId
            ?: throw IllegalStateException("Kan ikke distribuere brev uten journalpostId. BrevId=${brev.uuid}")

        return dokdistClient.distribuerJournalpost(
            request = DistribuerJournalpostRequest(
                journalpostId = journalPostId,
                bestillendeFagsystem = APP_NAVN,
                distribusjonstype = when (brev.type) {
                    BrevType.VEDTAKSBREV -> DistribuerJournalpostRequest.Distribusjonstype.VEDTAK
                    else -> DistribuerJournalpostRequest.Distribusjonstype.ANNET
                },
                dokumentProdApp = APP_NAVN,
                distribusjonstidspunkt = DistribuerJournalpostRequest.Distribusjonstidspunkt.UMIDDELBART
            )
        )
    }

    @PreAuthorize("hasAuthority('WRITE')")
    fun journalførIArkivet(
        journalPostId: EksternJournalpostId,
        fagsaksnummer: Saksnummer,
        journalfoerendeEnhet: Enhetsnummer,
        request: JournalforData,
    ) {
        dokarkivClient.oppdaterJournalpost(
            journalPostId = journalPostId,
            fagsaksnummer = fagsaksnummer,
            tittel = request.dokumenter.firstOrNull()?.tittel ?: "Ukjent innhold",
            bruker = request.bruker,
            avsender = request.avsender,
            dokumenter =
                request.dokumenter.map {
                    DokumentMedTittel(
                        tittel = it.tittel,
                        dokumentInfoId = it.dokumentInfoId,
                    )
                },
        )
        // Må sette logiske vedlegg i egen tjeneste
        request.dokumenter.forEach {
            dokarkivClient.setLogiskeVedlegg(
                dokumentInfoId = it.dokumentInfoId,
                titler = it.logiskeVedlegg ?: emptyList(),
            )
        }

        dokarkivClient.ferdigstill(journalPostId, journalfoerendeEnhet = journalfoerendeEnhet.value)
        logger.info("Journalført og ferdigstilt journalpost $journalPostId på fagsak $fagsaksnummer")
    }
}
