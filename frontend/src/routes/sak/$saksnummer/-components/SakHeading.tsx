import {Detail, Heading, HStack, VStack} from "@navikt/ds-react";
import {Sak} from "@generated";
import SakStatus from "~/routes/sak/$saksnummer/-components/SakStatus";
import BehandlingsMeny from "~/routes/sak/$saksnummer/-components/BehandlingsMeny";
import {useSuspenseQuery} from "@tanstack/react-query";
import {getKodeverkStonadsTypeOptions} from "~/routes/sak/$saksnummer/-api/sak.query";

interface Props {
    sak: Sak
}

export default function SakHeading({sak}: Props) {
    const {data: saksTyper} = useSuspenseQuery(getKodeverkStonadsTypeOptions())
    const sakType = saksTyper.find(type => type.type === sak.type)?.navn || sak.type
    return <HStack justify={"space-between"}>
        <VStack gap="space-4">
            <HStack gap={"space-8"}  align={"center"}>

                <Heading size={"small"}>{sak.beskrivelse}</Heading>
            </HStack>
            <HStack gap="space-8" align={"center"}>
                <Detail>{sakType}</Detail>
                <Detail textColor={"subtle"}>{sak.saksnummer}</Detail>
                <SakStatus sak={sak}/>

            </HStack>
        </VStack>
        <HStack gap={"space-8"} height={"1.5rem"}>
            <BehandlingsMeny sak={sak}/>
        </HStack>
    </HStack>
}