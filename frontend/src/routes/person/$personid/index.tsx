import {createFileRoute} from '@tanstack/react-router'
import {BodyShort, Box, Heading, Tabs, VStack} from '@navikt/ds-react'
import {PersonHeader} from "~/common/person/PersonHeader";
import {OppgaveTabell} from "~/common/oppgave/OppgaveTabell";
import {useSuspenseQuery} from "@tanstack/react-query";
import {hentOppgaverForPersonOptions} from "@generated/@tanstack/react-query.gen";
import {SakerUnderArbeidTabell} from "~/routes/person/$personid/-components/SakerUnderArbeidTabell";
import {SakerFerdigTabell} from "~/routes/person/$personid/-components/SakerFerdigTabell";

export const Route = createFileRoute('/person/$personid/')({
    component: PersonPage,
})

function PersonPage() {
    const {personid} = Route.useParams()

    const {data} = useSuspenseQuery(hentOppgaverForPersonOptions({path: {maskertPersonIdent: personid}}))
    const oppgaver = data?.filter(o => o.saksnummer === null) ?? []

    return (
        <VStack gap="space-24">
            <PersonHeader maskertPersonId={personid}/>
            <Heading size="large">Personside</Heading>
            <VStack gap="space-48">
                <Box background={"accent-soft"} padding="space-24" borderWidth="1" borderRadius="4">
                    <Heading size="medium">Saker under behandling</Heading>
                    <SakerUnderArbeidTabell maskertPersonIdent={personid}/>
                </Box>
                <Box background={"default"} borderRadius="4">
                    <Tabs defaultValue="saker">
                        <Tabs.List>
                            <Tabs.Tab value="saker" label="Sakshistorikk"/>
                            <Tabs.Tab value="oppgaver" label="Oppgaver"/>
                        </Tabs.List>

                        <Tabs.Panel value="saker">
                            <Box padding="space-16" borderWidth="1" borderRadius="2">
                                <VStack gap="space-16">
                                    <Heading size="small">Sakshistorikk</Heading>
                                    <BodyShort>Saker som er ferdig behandlet</BodyShort>
                                    <SakerFerdigTabell maskertPersonIdent={personid}/>
                                </VStack>
                            </Box>
                        </Tabs.Panel>
                        <Tabs.Panel value="oppgaver">
                            <Box padding="space-16" borderWidth="1" borderRadius="2">
                                <VStack gap="space-16">
                                    <Heading size="small">Åpne oppgaver</Heading>
                                    <BodyShort>Alle oppgaver på personen innen relevant tema. Oppgaver som er under
                                        behandling her vises ikke her</BodyShort>
                                    <OppgaveTabell oppgaver={oppgaver} dineOppgaver={false}/>
                                </VStack>
                            </Box>
                        </Tabs.Panel>
                    </Tabs>
                </Box>
            </VStack>
        </VStack>
    );
}
