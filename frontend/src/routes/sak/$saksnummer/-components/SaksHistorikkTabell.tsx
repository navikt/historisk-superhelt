import {useSuspenseQuery} from "@tanstack/react-query";
import {findSakerForPersonOptions} from "@generated/@tanstack/react-query.gen";
import {SakerTabell} from "~/common/sak/SakerTabell";


interface SakerTableProps {
    maskertPersonIdent: string

}

export function SaksHistorikkTabell({maskertPersonIdent}: SakerTableProps) {

    const {data, isPending, error} = useSuspenseQuery(({
        ...findSakerForPersonOptions({query: {maskertPersonId: maskertPersonIdent}})
    }))

    return <SakerTabell saker={data} isPending={isPending} error={error} hideSaksbehandler={true}
                        hideActions={true}/>
}

