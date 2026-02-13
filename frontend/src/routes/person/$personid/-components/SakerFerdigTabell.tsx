import {useSuspenseQuery} from "@tanstack/react-query";
import {findSakerForPersonOptions} from "@generated/@tanstack/react-query.gen";
import {SakerTabell} from "~/common/sak/SakerTabell";


interface SakerTableProps {
    maskertPersonIdent: string
}

export function SakerFerdigTabell({maskertPersonIdent}: SakerTableProps) {

    const {data, isPending, error} = useSuspenseQuery(({
        ...findSakerForPersonOptions({query: {maskertPersonId: maskertPersonIdent}}),
        retry: false,
    }))

    const saker = data.filter(sak => sak.status === "FERDIG")

    return <SakerTabell saker={saker} isPending={isPending} error={error}/>

}

