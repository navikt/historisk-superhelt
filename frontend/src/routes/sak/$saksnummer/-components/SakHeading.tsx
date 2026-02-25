import type {Sak} from "@generated";
import {Detail, Heading, HStack, Tag, VStack} from "@navikt/ds-react";
import {isSakFerdig} from "~/common/sak/sak.utils";
import BehandlingsMeny from "~/routes/sak/$saksnummer/-components/BehandlingsMeny";
import SakStatus from "~/routes/sak/$saksnummer/-components/SakStatus";
import {useStonadsTypeNavn} from "~/common/sak/useStonadsTypeNavn";

interface Props {
    sak: Sak
}

export default function SakHeading({sak}: Props) {
    const getStonadsTypeNavn = useStonadsTypeNavn()
    return <HStack justify={"space-between"}>
        <VStack gap="space-4">
            <HStack gap={"space-8"} align={"center"}>
                <Heading size={"small"}>{sak.beskrivelse}</Heading>
            </HStack>
            <HStack gap="space-8" align={"center"}>
                <Detail>{getStonadsTypeNavn(sak.type)}</Detail>
                <Detail textColor={"subtle"}>{sak.saksnummer}</Detail>
                <SakStatus sak={sak}/>
                {!isSakFerdig(sak) && sak.gjenapnet &&
                    <Tag variant="moderate" size="small" data-color="success">Gjenåpnet</Tag>}
            </HStack>
        </VStack>
        <HStack gap={"space-8"} height={"1.5rem"}>
            <BehandlingsMeny sak={sak}/>
        </HStack>
    </HStack>
}