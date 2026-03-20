import {getKodeverkOppgaveGjelderOptions} from "@generated/@tanstack/react-query.gen";
import {useSuspenseQuery} from "@tanstack/react-query";
import type {OppgaveGjelder} from "./oppgave.types";

export function useOppgaveGjelderNavn() {
    const {data} = useSuspenseQuery(getKodeverkOppgaveGjelderOptions());
    return (oppgaveGjelder: OppgaveGjelder) => data.find((s) => s.type === oppgaveGjelder)?.navn ?? oppgaveGjelder;
}
