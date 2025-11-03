import {BodyShort, HStack, VStack} from "@navikt/ds-react";
import {Sak} from "@api";
import SakStatus from "~/routes/sak/$saksnummer/-components/SakStatus";

interface Props {
    sak: Sak
}

export default function SakHeading({sak}: Props) {


    return <VStack>
        <HStack align={"center"} gap="space-4">
            <SakStatus sak={sak}/>
            <BodyShort truncate>{sak.saksnummer}/{sak.tittel}</BodyShort>
        </HStack>
    </VStack>
}