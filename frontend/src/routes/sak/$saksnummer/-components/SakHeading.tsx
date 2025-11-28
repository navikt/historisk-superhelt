import {Detail, Heading, HStack, VStack} from "@navikt/ds-react";
import {Sak} from "@generated";
import SakStatus from "~/routes/sak/$saksnummer/-components/SakStatus";
import SakMeny from "~/routes/sak/$saksnummer/-components/SakMeny";

interface Props {
    sak: Sak
}

export default function SakHeading({sak}: Props) {

    return <HStack justify={"space-between"}>
        <VStack gap="space-4">
            <Detail textColor={"default"}>Sak:{sak.saksnummer}</Detail>
            <SakStatus sak={sak}/>
            <Heading size={"small"}>{sak.tittel}</Heading>
        </VStack>
        <HStack gap={"space-8"} height={"1.5rem"}>
            <SakMeny sak={sak}/>
        </HStack>
    </HStack>
}