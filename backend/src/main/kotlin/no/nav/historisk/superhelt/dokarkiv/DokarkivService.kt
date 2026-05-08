package no.nav.historisk.superhelt.dokarkiv

import no.nav.common.consts.APP_NAVN
import no.nav.common.consts.FellesKodeverkTema
import no.nav.common.types.EksternJournalpostId
import no.nav.common.types.Enhetsnummer
import no.nav.common.types.Saksnummer
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
import no.nav.historisk.superhelt.brev.Brev
import no.nav.historisk.superhelt.sak.Sak
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class DokarkivService(
    private val dokarkivClient: DokarkivClient,
    private val avsenderMottakerResolver: AvsenderMottakerResolver,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PreAuthorize("hasAuthority('WRITE')")
    fun arkiver(sak: Sak, brev: Brev, pdf: ByteArray): JournalpostResponse {

        val req = JournalpostRequest(
            tittel = brev.tittel!!,
            journalpostType = JournalpostType.UTGAAENDE,
            tema = sak.type.tema,
            avsenderMottaker = avsenderMottakerResolver.resolve(brev.mottakerType, sak),
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
            journalfoerendeEnhet = sak.type.enhet
        )
        return dokarkivClient.opprett(req, forsokFerdigstill = true)
    }

    @PreAuthorize("hasAuthority('WRITE')")
    fun journalførIArkivet(
        journalPostId: EksternJournalpostId,
        fagsaksnummer: Saksnummer,
        journalfoerendeEnhet: Enhetsnummer,
        tema: FellesKodeverkTema,
        request: JournalforData,
    ) {
        dokarkivClient.oppdaterJournalpost(
            journalPostId = journalPostId,
            fagsaksnummer = fagsaksnummer,
            tittel = request.dokumenter.firstOrNull()?.tittel ?: "Ukjent innhold",
            bruker = request.bruker,
            avsender = request.avsender,
            tema = tema,
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
