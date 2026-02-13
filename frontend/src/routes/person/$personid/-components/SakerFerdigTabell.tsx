import {useSuspenseQuery} from "@tanstack/react-query";
import {findSakerForPersonOptions} from "@generated/@tanstack/react-query.gen";
import {SakerTabell} from "~/common/sak/SakerTabell";
import {isSakFerdig} from "~/routes/person/$personid/-components/sak.utils";


interface SakerTableProps {
    maskertPersonIdent: string
}



export function SakerFerdigTabell({maskertPersonIdent}: SakerTableProps) {

    const {data, isPending, error} = useSuspenseQuery(({
        ...findSakerForPersonOptions({query: {maskertPersonId: maskertPersonIdent}}),
        retry: false,
    }))

    const saker = data.filter(isSakFerdig)

    return <SakerTabell saker={saker} isPending={isPending} error={error}/>

}

