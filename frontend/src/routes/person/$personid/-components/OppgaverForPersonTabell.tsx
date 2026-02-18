import {hentOppgaverForPersonOptions} from "@generated/@tanstack/react-query.gen";
import {useSuspenseQuery} from "@tanstack/react-query";
import {OppgaveTabell} from "~/common/oppgave/OppgaveTabell";

interface OppgaverTableProps {
    maskertPersonIdent: string
}

export function OppgaverForPersonTabell({maskertPersonIdent}: OppgaverTableProps) {

    const {data} = useSuspenseQuery(hentOppgaverForPersonOptions({path: {maskertPersonIdent: maskertPersonIdent}}))
    const oppgaver = data?.filter(o => o.saksnummer === null) ?? []

    return <OppgaveTabell oppgaver={oppgaver} dineOppgaver={false}/>
}