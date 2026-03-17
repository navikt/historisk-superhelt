import { BodyShort, Box, Heading, Tabs, VStack } from "@navikt/ds-react";
import { useSuspenseQuery } from "@tanstack/react-query";
import { createFileRoute } from "@tanstack/react-router";
import { useEffect } from "react";
import { RfcErrorBoundary } from "~/common/error/RfcErrorBoundary";
import { PersonHeader } from "~/common/person/PersonHeader";
import { finnPersonQuery } from "~/common/person/person.query";
import { kortNavn } from "~/common/string.utils";
import { OppgaverForPersonTabell } from "~/routes/person/$personid/-components/OppgaverForPersonTabell";
import { SakerUnderArbeidTabell } from "~/routes/person/$personid/-components/SakerUnderArbeidTabell";
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
        <VStack gap="space-24">
            <PersonHeader maskertPersonId={personid} />
            <RfcErrorBoundary key={personid}>
                <Heading size="large">Personside</Heading>
                <VStack gap="space-48">
                    <Box background={"accent-soft"} padding="space-24" borderWidth="1" borderRadius="4">
                        <Heading size="medium">Saker under behandling</Heading>
                        <SakerUnderArbeidTabell maskertPersonIdent={personid} />
                    </Box>
                    <Box background={"default"} borderRadius="4">
                        <Tabs defaultValue="saker">
                            <Tabs.List>
                                <Tabs.Tab value="saker" label="Sakshistorikk" />
                                <Tabs.Tab value="oppgaver" label="Oppgaver" />
                            </Tabs.List>

                            <Tabs.Panel value="saker">
                                <Box padding="space-16" borderWidth="1" borderRadius="2">
                                    <VStack gap="space-16">
                                        <Heading size="small">Sakshistorikk</Heading>
                                        <BodyShort>Saker som er ferdig behandlet</BodyShort>
                                        <SakshistorikkPersonTabell maskertPersonIdent={personid} />
                                    </VStack>
                                </Box>
                            </Tabs.Panel>
                            <Tabs.Panel value="oppgaver">
                                <Box padding="space-16" borderWidth="1" borderRadius="2">
                                    <VStack gap="space-16">
                                        <Heading size="small">Åpne oppgaver</Heading>
                                        <BodyShort>
                                            Alle oppgaver på personen innen relevant tema. Oppgaver som er under
                                            behandling her vises ikke her
                                        </BodyShort>
                                        <OppgaverForPersonTabell maskertPersonIdent={personid} />
                                    </VStack>
                                </Box>
                            </Tabs.Panel>
                        </Tabs>
                    </Box>
                </VStack>
            </RfcErrorBoundary>
        </VStack>
    );
}
