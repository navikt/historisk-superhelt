import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useQuery, useSuspenseQuery } from "@tanstack/react-query";
import { getSakOptions } from "~/routes/sak/$saksnummer/-api/sak.query";
import { BrevEditor } from "~/routes/sak/$saksnummer/-components/BrevEditor";
import { getOrCreateBrevOptions } from "~/routes/sak/$saksnummer/-api/brev.query";
import { finnPersonQuery } from "~/common/person/person.query";

export const Route = createFileRoute("/sak/$saksnummer/vedtaksbrevbruker")({
    component: BrevPage,
});

function BrevPage() {
    const { saksnummer } = Route.useParams();
    const { data: sak } = useSuspenseQuery(getSakOptions(saksnummer));
    const { data: person } = useSuspenseQuery(finnPersonQuery(sak.maskertPersonIdent));
    const hasSaksbehandleRettighet = sak.rettigheter.includes("SAKSBEHANDLE");
    const { data: brev } = useQuery({
        ...getOrCreateBrevOptions(saksnummer, "VEDTAKSBREV", person.harVerge ? "VERGE" : "BRUKER"),
    });
    const navigate = useNavigate();

    const gotoOppsummering = () => navigate({ to: "/sak/$saksnummer/oppsummering", params: { saksnummer } });

    const brevId = brev?.uuid;

    return (
        <BrevEditor
            sak={sak}
            brevId={brevId}
            buttonText="Lagre og gå videre"
            readOnly={!hasSaksbehandleRettighet}
            onSuccess={gotoOppsummering}
        />
    );
}
