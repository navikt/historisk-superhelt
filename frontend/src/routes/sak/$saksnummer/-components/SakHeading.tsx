import type {Sak} from "@generated";
import {Detail, Heading, HStack, Tag, VStack} from "@navikt/ds-react";
import {useSuspenseQuery} from "@tanstack/react-query";
import {isSakFerdig} from "~/common/sak/sak.utils";
import {getKodeverkStonadsTypeOptions} from "~/routes/sak/$saksnummer/-api/sak.query";
import BehandlingsMeny from "~/routes/sak/$saksnummer/-components/BehandlingsMeny";
import SakStatus from "~/routes/sak/$saksnummer/-components/SakStatus";

interface Props {
    sak: Sak
}

export default function SakHeading({sak}: Props) {
    const {data: saksTyper} = useSuspenseQuery(getKodeverkStonadsTypeOptions())
    const sakType = saksTyper.find(type => type.type === sak.type)?.navn || sak.type
    return <HStack justify={"space-between"}>
        <VStack gap="space-4">
            <HStack gap={"space-8"} align={"center"}>
                <Heading size={"small"}>{sak.beskrivelse}</Heading>
            </HStack>
            <HStack gap="space-8" align={"center"}>
                <Detail>{sakType}</Detail>
                <Detail textColor={"subtle"}>{sak.saksnummer}</Detail>
                <SakStatus sak={sak}/>
                {!isSakFerdig(sak) && sak.gjenapnet && <Tag variant="moderate" size="small" data-color="success">Gjenåpnet</Tag>}
            </HStack>
        </VStack>
        <HStack gap={"space-8"} height={"1.5rem"}>
            <BehandlingsMeny sak={sak}/>
        </HStack>
    </HStack>
}