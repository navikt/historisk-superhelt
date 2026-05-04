import {useSuspenseQuery} from "@tanstack/react-query";
import {createFileRoute} from "@tanstack/react-router";
import {ErrorAlert} from "~/common/error/ErrorAlert";
import {getSakOptions} from "~/common/sak/sak.query";
import SakOpplysningerSummary from "~/routes/sak/$saksnummer/-components/SakOpplysningerSummary";
import SakOpplysningerEditor from "./-components/SakOpplysningerEditor";

export const Route = createFileRoute("/sak/$saksnummer/opplysninger")({
    component: OpplysningerPage,
    errorComponent: ({ error }) => {
        return <ErrorAlert error={error} />;
    },
});

function OpplysningerPage() {
    const { saksnummer } = Route.useParams();
    const { data: sak } = useSuspenseQuery(getSakOptions(saksnummer));
    if (sak?.rettigheter.includes("SAKSBEHANDLE")) {
        return <SakOpplysningerEditor sak={sak} />;
    }
    return <SakOpplysningerSummary sak={sak} />;
}
