import type { Sak } from "@generated";
import { getSakStatusOptions } from "@generated/@tanstack/react-query.gen";
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
                            return "Saken er innvilget og ferdigstilt";
                        case "DELVIS_INNVILGET":
                            return "Saken er delvis innvilget og ferdigstilt";
                        case "AVSLATT":
                            return "Saken er avslått og ferdigstilt";
                        default:
                            return "Saken er ferdigstilt";
                    }
                };
                return (
                    <>
                        <VStack gap="space-2">
                            <Heading size={"medium"}>{ferdigTekst()}</Heading>
                            {(sak.vedtaksResultat === "INNVILGET" || sak.vedtaksResultat === "DELVIS_INNVILGET") && (
                                <UtbetalingStatusVis sak={sak} />
                            )}
                        </VStack>
                        <SakErrorSummary sak={sak} />
                    </>
                );
            }
        }
    }

    return <VStack gap={"space-32"}>{renderAction()}</VStack>;
}

function UtbetalingStatusVis({ sak }: { sak: Sak }) {
    const { data: sakStatus } = useSuspenseQuery(getSakStatusOptions({ path: { saksnummer: sak.saksnummer } }));

    const tekst = (() => {
        switch (sakStatus.utbetalingStatus) {
            case "UTBETALT":
                return "Utbetaling er gjennomført";
            case "BEHANDLET_AV_UTBETALING":
            case "MOTTATT_AV_UTBETALING":
            case "SENDT_TIL_UTBETALING":
                return "Utbetaling pågår";
            case "KLAR_TIL_UTBETALING":
            case "UTKAST":
                return "Venter på utbetaling";
            default:
                return null;
        }
    })();

    if (!tekst) return null;
    return <BodyShort>{tekst}</BodyShort>;
}
