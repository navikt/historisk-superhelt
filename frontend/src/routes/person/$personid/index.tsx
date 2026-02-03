import {createFileRoute, useNavigate} from '@tanstack/react-router'
import {Box, Heading, HStack, Tabs, VStack} from '@navikt/ds-react'
import {RfcErrorBoundary} from "~/common/error/RfcErrorBoundary";
import {SakerTable} from "./-components/SakerTable";
import {PersonHeader} from "~/common/person/PersonHeader";
import {useSuspenseQuery} from "@tanstack/react-query";
import {createSak} from "@generated";
import {finnPersonQuery} from "~/common/person/person.query";

export const Route = createFileRoute('/person/$personid/')({
    component: PersonPage,
})

function PersonPage() {
    const {personid} = Route.useParams()
    const {data: person} = useSuspenseQuery(finnPersonQuery(personid))

    const navigate = useNavigate()


    async function opprettSak() {
        const {data, error: apiError} = await createSak({
            body: {
                type: "PARYKK",
                fnr: person.fnr,
            }
        })
        navigate({to: `/sak/${data?.saksnummer}`})
    }

    return (
        <VStack gap="6">
            <PersonHeader maskertPersonId={personid}/>
            <Heading size="xlarge">Personside</Heading>
            <RfcErrorBoundary>
                <Tabs defaultValue="saker">
                    <Tabs.List>
                        <Tabs.Tab value="saker" label="Saker"/>
                    </Tabs.List>

                    <Tabs.Panel value="saker">
                        <Box padding="4" borderWidth="1" borderRadius="small">
                            <VStack gap="4">
                                <HStack justify="space-between" align="center">
                                    <Heading size="medium">Relevante saker</Heading>

                                    {/*<Button size="small" variant="primary" icon={<PlusIcon/>} onClick={opprettSak}>*/}
                                    {/*    Opprett ny sak*/}
                                    {/*</Button>*/}
                                </HStack>
                                <SakerTable maskertPersonIdent={personid}/>
                            </VStack>
                        </Box>
                    </Tabs.Panel>
                </Tabs>
            </RfcErrorBoundary>
        </VStack>
    )
}
