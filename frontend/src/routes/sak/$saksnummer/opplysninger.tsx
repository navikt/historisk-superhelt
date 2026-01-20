import {createFileRoute} from '@tanstack/react-router'
import {useSuspenseQuery} from "@tanstack/react-query";
import {getSakOptions} from "./-api/sak.query";
import {ErrorAlert} from "~/common/error/ErrorAlert";
import SakEditor from "~/routes/sak/$saksnummer/-components/SakEditor";
import SakSummary from "~/routes/sak/$saksnummer/-components/SakSummary";


export const Route = createFileRoute('/sak/$saksnummer/opplysninger')({
    component: OpplysningerPage,
    errorComponent: ({error}) => {
        return <ErrorAlert error={error}/>
    }
})


function OpplysningerPage() {
    const {saksnummer} = Route.useParams()
    const {data: sak} = useSuspenseQuery(getSakOptions(saksnummer))
    if (sak?.rettigheter.includes("SAKSBEHANDLE")) {
        return <SakEditor sak={sak}/>
    }
    return (
        <SakSummary sak={sak}/>
    )
}
