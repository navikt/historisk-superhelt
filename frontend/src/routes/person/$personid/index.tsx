import { BodyShort, Heading, Page, VStack } from "@navikt/ds-react";
import { useSuspenseQuery } from "@tanstack/react-query";
import { createFileRoute } from "@tanstack/react-router";
import { useEffect } from "react";
import { Card } from "~/common/card/Card";
import { RfcErrorBoundary } from "~/common/error/RfcErrorBoundary";
import { PersonHeader } from "~/common/person/PersonHeader";
import { finnPersonQuery } from "~/common/person/person.query";
import { SakshistorikkKombinertTabell } from "~/common/sak/historikk/SakshistorikkKombinertTabell";
import { useSakHistorikk } from "~/common/sak/historikk/useSaksHistorikk";
import { isSakFerdig } from "~/common/sak/sak.utils";
import { kortNavn } from "~/common/string.utils";
import { OppgaverForPersonTabell } from "~/routes/person/$personid/-components/OppgaverForPersonTabell";

export const Route = createFileRoute("/person/$personid/")({
    component: PersonPage,
});

function PersonPage() {
    const { personid } = Route.useParams();
    const { data: person } = useSuspenseQuery(finnPersonQuery(personid));
    const { result: sakHistorikkResult } = useSakHistorikk({
        maskertPersonIdent: personid,
        filter: (sak) => isSakFerdig(sak),
    });

    useEffect(() => {
        document.title = kortNavn(person.navn);
        return () => {
            document.title = "Superhelt";
        };
    }, [person.navn]);

    return (
        <>
            <PersonHeader maskertPersonId={personid} />
            <Page.Block width="2xl">
                <VStack paddingBlock="space-24" gap="space-32">
                    <RfcErrorBoundary key={personid}>
                        <Heading size="large">Saksoversikt for {person.navn}</Heading>
                        <Card>
                            <Heading size="medium">Under behandling</Heading>
                            <OppgaverForPersonTabell maskertPersonIdent={personid} />
                        </Card>
                        <Card>
                            <Heading size="medium">Sakshistorikk</Heading>
                            <BodyShort>Saker som er ferdig behandlet</BodyShort>
                            <SakshistorikkKombinertTabell {...sakHistorikkResult} size="large" />
                        </Card>
                    </RfcErrorBoundary>
                </VStack>
            </Page.Block>
        </>
    );
}
