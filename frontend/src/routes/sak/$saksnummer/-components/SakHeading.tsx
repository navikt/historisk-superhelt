import {Detail, Heading, HStack} from "@navikt/ds-react";
import {Sak} from "@generated";
import SakStatus from "~/routes/sak/$saksnummer/-components/SakStatus";
import SakMeny from "~/routes/sak/$saksnummer/-components/SakMeny";

interface Props {
    sak: Sak
}

export default function SakHeading({sak}: Props) {

    return <HStack justify={"space-between"}>
        <HStack align={"center"} gap="space-4">
            <Detail textColor={"default"}>{sak.saksnummer}</Detail>
            <Heading size={"small"}>{sak.tittel}</Heading>
            <SakStatus sak={sak}/>
        </HStack>
        <SakMeny sak={sak}/>
    </HStack>
}