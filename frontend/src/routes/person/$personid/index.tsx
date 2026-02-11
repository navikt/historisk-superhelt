import {createFileRoute} from '@tanstack/react-router'
import {Box, Heading, HStack, Tabs, VStack} from '@navikt/ds-react'
import {RfcErrorBoundary} from "~/common/error/RfcErrorBoundary";
import {SakerTable} from "./-components/SakerTable";
import {PersonHeader} from "~/common/person/PersonHeader";

export const Route = createFileRoute('/person/$personid/')({
    component: PersonPage,
})

function PersonPage() {
    const {personid} = Route.useParams()

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
