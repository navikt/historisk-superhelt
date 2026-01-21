package no.nav.historisk.superhelt.dokarkiv

import no.nav.common.types.EksternJournalpostId
import no.nav.dokarkiv.EksternDokumentInfoId
import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.saf.graphql.Journalpost
import no.nav.saf.graphql.SafGraphqlClient
import no.nav.saf.rest.DokumentResponse
import no.nav.saf.rest.SafRestClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class JournalpostService(
    private val safRestClient: SafRestClient,
    private val safGraphqlClient: SafGraphqlClient,
    ) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun lastNedEttDokument(
        journalpostId: EksternJournalpostId,
        dokumentId: EksternDokumentInfoId,
    ): DokumentResponse {
        val journalpost =
            hentJournalpost(journalpostId)
                ?: throw IkkeFunnetException("Finner ikke journalpost med id $journalpostId")
        journalpost.dokumenter?.firstOrNull { it.dokumentInfoId == dokumentId }
            ?: throw IkkeFunnetException("Finner ikke dokument med id $dokumentId i journalpost $journalpostId")
        return hentDokument(journalpostId, dokumentId)
    }

    private fun hentDokument(
        journalpostId: EksternJournalpostId,
        dokumentId: EksternDokumentInfoId,
    ): DokumentResponse = safRestClient.hentDokument(journalpostId, dokumentId)


    fun hentJournalpost(journalpostId: EksternJournalpostId): Journalpost? {
        val journalpost = safGraphqlClient.hentJournalpost(journalpostId).data?.journalpost
        validerTilgang(journalpost)
        return journalpost
    }

//    fun hentJournalpostMedBehandlingsnummer(behandlingsnummer: Saksnummer): Journalpost? {
//        val journalpostId =
//            oppgaveComponent
//                .hentOppgaveForBehandlingOgOppgaveType(
//                    behandlingsnummer,
//                    OppgaveType.JFR,
//                )?.journalpostId ?: throw IllegalStateException("Fant ikke journalpost for søknaden")
//
//        val journalpost = safGraphqlClient.hentJournalpost(journalpostId).data?.journalpost
//        validerTilgang(journalpost)
//        return journalpost
//    }

    private fun validerTilgang(journalpost: Journalpost?) {
//        val bruker = journalpost?.bruker
//
//        // TODO Journalpost-apiet gjør også tilgangskontroll og gir resultat basert på det tilbake. Tilgangen er mer fingranulert enn det vi gjør her.  Det er ikke sikkert vi trenger å gjøre dette her?
//        val id =
//            bruker?.id
//                ?: throw IllegalArgumentException("Ukjent eller manglende brukerinfo $bruker for journalpost ${journalpost?.journalpostId}")
//        val personident =
//            when (bruker.type) {
//                BrukerIdType.FNR -> Personident(id)
//                BrukerIdType.AKTOERID -> persondataFacade.hentPersonForAktør(AktørId(id)).fnr
//                else -> throw IllegalArgumentException("Ukjent eller manglende brukerinfo $bruker for journalpost ${journalpost.journalpostId}")
//            }
//
//        tilgangskontrollService.validerTilgangTilPerson(personident)
    }



}