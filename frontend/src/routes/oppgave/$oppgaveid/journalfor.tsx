import {createFileRoute} from '@tanstack/react-router'
import {Heading, VStack} from "@navikt/ds-react";
import {useSuspenseQuery} from "@tanstack/react-query";
import {getOppgaveOptions, getUserInfoOptions} from "@generated/@tanstack/react-query.gen";
import {OppgaveGjelder} from "~/routes/oppgave/$oppgaveid/-types/oppgave.types";
import {StonadType} from "~/routes/sak/$saksnummer/-types/sak.types";
import {FerdigJournalfort} from "~/routes/oppgave/$oppgaveid/-components/FerdigJournalfort";
import {JournalforForm} from "~/routes/oppgave/$oppgaveid/-components/JournalforForm";
import {finnPersonQuery} from "~/common/person/person.query";
import {hentJournalpostMetadataQuery} from "~/routes/oppgave/$oppgaveid/-api/journalpost.query";

export const Route = createFileRoute('/oppgave/$oppgaveid/journalfor')({
    component: JournalforPage,
})

function guessStonadsType(oppgaveGjelder: OppgaveGjelder): StonadType | undefined {


    switch (oppgaveGjelder) {
        case "ANSIKTSDEFEKTSPROTESE":
            return "ANSIKT_PROTESE"

        case "BRYSTPROTESE_PROTESEBH":
            return "BRYSTPROTESE"

        case "OYEPROTESE":
            return "OYE_PROTESE"

        case "PARYKK_HODEPLAGG":
            return "PARYKK"

        case "REISEUTGIFTER":
        case "REISEPENGER_UTPROVING_ORT_TEKNISKE_HJELPEMIDLER":
            return "REISEUTGIFTER"

        case "FORNYELSESSOKNAD_ORTOPEDISKE_HJELPEMIDLER":
        case "ORTOPEDISKE_HJELPEMIDLER_SOKNAD":
        case "ORTOPEDISKE_HJELPEMIDLER_UTLAND":
        case "ORTOPEDISKE_HJELPEMIDLER":
            return "FOTSENG"

        case "BIDRAG_EKSKL_FARSKAP":
        case "ANKE":
        case "KLAGE":
        case "TIDLIGERE_HJEMSENDT_SAK":
        case "HJEMSENDT_TIL_NY_BEHANDLING":
        case "PARTSINNSYN":
        case "MEDLEMSKAP":
        case "UKJENT":
            return undefined

    }
}

function JournalforPage() {
    const oppgaveId = Route.useParams().oppgaveid;
    const {data: oppgave} = useSuspenseQuery(getOppgaveOptions({path: {oppgaveId: Number(oppgaveId)}}))
    const {data: person} = useSuspenseQuery(finnPersonQuery(oppgave.maskertPersonIdent))
    const {data: journalPost} = useSuspenseQuery(hentJournalpostMetadataQuery(oppgave.journalpostId))
    const {data: user} = useSuspenseQuery(getUserInfoOptions())
    const harSkriveTilgang = user.roles.includes('SAKSBEHANDLER')

    const completed =
        oppgave.oppgavestatus === 'FERDIGSTILT' &&
        !!oppgave.saksnummer

    function oppdaterBruker() {
       // TODO hvordan bytte bruker i header
        console.log("Bruker oppdatert")
    }

    return (
        <VStack gap={"space-24"}>
            <Heading size="xlarge">Journalfør oppgave {oppgave.oppgaveId}</Heading>

            <>
                {completed && <FerdigJournalfort saksnummer={oppgave.saksnummer}/>}
                {!completed && (
                    <JournalforForm
                        person={person}
                        oppgaveMedSak={oppgave}
                        journalPost={journalPost}
                        readOnly={!harSkriveTilgang}
                        defaultStonadstype={guessStonadsType(oppgave.oppgaveGjelder)}
                        onBrukerUpdate={oppdaterBruker}
                    />
                )}
        </>
        </VStack>
    );
}
