import { BodyShort, Heading, Page, VStack } from "@navikt/ds-react";
import { useSuspenseQuery } from "@tanstack/react-query";
import { createFileRoute } from "@tanstack/react-router";
import { useEffect } from "react";
import { Card } from "~/common/card/Card";
import { RfcErrorBoundary } from "~/common/error/RfcErrorBoundary";
import { PersonHeader } from "~/common/person/PersonHeader";
import { finnPersonQuery } from "~/common/person/person.query";
import { kortNavn } from "~/common/string.utils";
import { OppgaverForPersonTabell } from "~/routes/person/$personid/-components/OppgaverForPersonTabell";
import { SakshistorikkPersonTabell } from "~/routes/person/$personid/-components/SakshistorikkPersonTabell";

export const Route = createFileRoute("/person/$personid/")({
    component: PersonPage,
});

function PersonPage() {
    const { personid } = Route.useParams();
    const { data: person } = useSuspenseQuery(finnPersonQuery(personid));

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
                        <Heading size="large">Personside</Heading>
                        <Card>
                            <Heading size="medium">Under behandling</Heading>
                            <OppgaverForPersonTabell maskertPersonIdent={personid} />
                        </Card>
                        <Card>
                            <Heading size="medium">Sakshistorikk</Heading>
                            <BodyShort>Saker som er ferdig behandlet</BodyShort>
                            <SakshistorikkPersonTabell maskertPersonIdent={personid} />
                        </Card>
                    </RfcErrorBoundary>
                </VStack>
            </Page.Block>
        </>
    );
}
