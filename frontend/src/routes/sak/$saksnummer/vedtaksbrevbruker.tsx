import {createFileRoute, useNavigate} from '@tanstack/react-router'
import {useSuspenseQuery} from "@tanstack/react-query";
import {getSakOptions} from "~/routes/sak/$saksnummer/-api/sak.query";
import {VedtaksBrevEditor} from "~/routes/sak/$saksnummer/-components/VedtaksBrevEditor";

export const Route = createFileRoute('/sak/$saksnummer/vedtaksbrevbruker')({
    component: BrevPage,
})


function BrevPage() {
    const {saksnummer} = Route.useParams()
    const {data: sak} = useSuspenseQuery(getSakOptions(saksnummer))
    const navigate = useNavigate()

    const hasSaksbehandleRettighet = sak.rettigheter.includes("SAKSBEHANDLE")
    const gotoOppsummering = () => navigate({to: "/sak/$saksnummer/oppsummering", params: {saksnummer}});

    return <VedtaksBrevEditor sak={sak}
                              type={"VEDTAKSBREV"}
                              mottaker="BRUKER"
                              buttonText="Lagre og gå videre"
                              readOnly={!hasSaksbehandleRettighet}
                              onSuccess={gotoOppsummering}/>


}


