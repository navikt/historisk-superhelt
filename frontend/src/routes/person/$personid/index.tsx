import {createFileRoute, useNavigate} from '@tanstack/react-router'
import {Box, Button, Heading, HStack, Tabs, VStack} from '@navikt/ds-react'
import {PlusIcon} from '@navikt/aksel-icons'
import {RfcErrorBoundary} from "~/components/error/RfcErrorBoundary";
import {SakerTable} from "./-components/SakerTable";
import {PersonHeader} from "~/components/PersonHeader";
import {DokumenterTable} from "~/routes/person/$personid/-components/DokumenterTable";
import {useSuspenseQuery} from "@tanstack/react-query";
import {getPersonByMaskertIdentOptions} from "@api/@tanstack/react-query.gen";
import {createSak} from "@api";

export const Route = createFileRoute('/person/$personid/')({
    component: PersonPage,
})

function PersonPage() {
    const {personid} = Route.useParams()
    const {data: person} = useSuspenseQuery(
        {
            ...getPersonByMaskertIdentOptions({
                path: {
                    maskertPersonident: personid
                }
            })
        })
    const navigate = useNavigate()


    async function opprettSak() {
        const {data, error: apiError} = await createSak({
            body: {
                type: "PARYKK",
                fnr: person?.fnr!,
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
                        <Tabs.Tab value="dokumenter" label="Dokumenter"/>
                    </Tabs.List>

                    <Tabs.Panel value="saker">
                        <Box padding="4" borderWidth="1" borderRadius="small">
                            <VStack gap="4">
                                <HStack justify="space-between" align="center">
                                    <Heading size="medium">Relevante saker</Heading>

                                    <Button size="small" variant="primary" icon={<PlusIcon/>} onClick={opprettSak}>
                                        Opprett ny sak
                                    </Button>

                                </HStack>

                                <SakerTable person={personid}/>
                            </VStack>
                        </Box>
                    </Tabs.Panel>

                    <Tabs.Panel value="dokumenter">
                        <Box padding="4" borderWidth="1" borderRadius="small">
                            <VStack gap="4">
                                <Heading size="medium">Dokumenter</Heading>
                                <DokumenterTable/>
                            </VStack>
                        </Box>
                    </Tabs.Panel>
                </Tabs>
            </RfcErrorBoundary>
        </VStack>
    )
}
