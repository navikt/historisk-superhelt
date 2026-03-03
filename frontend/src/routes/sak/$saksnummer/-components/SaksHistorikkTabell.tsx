import {useSuspenseQuery} from "@tanstack/react-query";
import {findSakerForPersonOptions} from "@generated/@tanstack/react-query.gen";
import {SakerTabell} from "~/common/sak/SakerTabell";
import {isSakFerdig} from "~/common/sak/sak.utils";


interface SakerTableProps {
    maskertPersonIdent: string

}

export function SaksHistorikkTabell({maskertPersonIdent}: SakerTableProps) {

    const {data, isPending, error} = useSuspenseQuery(({
        ...findSakerForPersonOptions({query: {maskertPersonId: maskertPersonIdent}})
    }))

    //todo lik SakerFerdig filter
    const saker = data.filter(sak => sak.status === "FERDIG")

    return <SakerTabell saker={saker} isPending={isPending} error={error} hideSaksbehandler={true}
                        hideActions={true}/>
}

