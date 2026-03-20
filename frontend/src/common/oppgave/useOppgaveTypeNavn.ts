import {getKodeverkOppgaveTypeOptions} from "@generated/@tanstack/react-query.gen";
import {useSuspenseQuery} from "@tanstack/react-query";
import type {OppgaveType} from "./oppgave.types";

export function useOppgaveTypeNavn() {
    const { data } = useSuspenseQuery(getKodeverkOppgaveTypeOptions());
    return (oppgaveType: OppgaveType) => data.find((verdi) => verdi.type === oppgaveType)?.navn ?? oppgaveType;
}
