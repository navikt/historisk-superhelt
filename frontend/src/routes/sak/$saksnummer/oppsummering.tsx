import { BodyShort, Heading, InfoCard, VStack } from "@navikt/ds-react";
import { useSuspenseQuery } from "@tanstack/react-query";
import { createFileRoute } from "@tanstack/react-router";
import { getSakOptions } from "~/routes/sak/$saksnummer/-api/sak.query";
import AttesterSakAction from "~/routes/sak/$saksnummer/-components/AttesterSakAction";
import SakErrorSummary from "~/routes/sak/$saksnummer/-components/SakErrorSummary";
import TotrinnkontrollAction from "~/routes/sak/$saksnummer/-components/TotrinnkontrollAction";

export const Route = createFileRoute("/sak/$saksnummer/oppsummering")({
    component: OppsummeringPage,
});

function OppsummeringPage() {
    const { saksnummer } = Route.useParams();
    const { data: sak } = useSuspenseQuery(getSakOptions(saksnummer));

    function renderAction() {
        switch (sak.status) {
            case "FEILREGISTRERT":
                return <Heading size={"medium"}>Saken er feilregistert</Heading>;
            case "UNDER_BEHANDLING":
                return <TotrinnkontrollAction sak={sak} />;
            case "TIL_ATTESTERING":
                return <AttesterSakAction sak={sak} />;
            case "FERDIG_ATTESTERT":
                return (
                    <>
                        <InfoCard data-color={"warning"}>
                            <InfoCard.Header>
                                <InfoCard.Title>Saken er ferdig attestert men ikke fullført</InfoCard.Title>
                            </InfoCard.Header>
                            <InfoCard.Content>
                                Denne statusen bør være midlertidig. Ta kontakt med support hvis saken forblir i denne
                                statusen over lengre tid.
                            </InfoCard.Content>
                        </InfoCard>
                        <SakErrorSummary sak={sak} />
                    </>
                );
            case "FERDIG": {
                const ferdigTekst = () => {
                    switch (sak.vedtaksResultat) {
                        case "HENLAGT":
                            return "Saken er henlagt";
                        case "INNVILGET":
                        case "DELVIS_INNVILGET":
                            return "Saken er godkjent";
                        case "AVSLATT":
                            return "Saken er ikke godkjent";
                        default:
                            return "Saken er ferdigstilt";
                    }
                };
                return (
                    <>
                        <VStack gap="space-2">
                            <Heading size={"medium"}>{ferdigTekst()}</Heading>
                            <BodyShort>Saksbehandler: {sak.saksbehandler.navn}</BodyShort>
                            {sak.attestant && <BodyShort>Attestant: {sak.attestant.navn}</BodyShort>}
                        </VStack>
                        <SakErrorSummary sak={sak} />
                    </>
                );
            }
        }
    }

    return <VStack gap={"space-32"}>{renderAction()}</VStack>;
}
