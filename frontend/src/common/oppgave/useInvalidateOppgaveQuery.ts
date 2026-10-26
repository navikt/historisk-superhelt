import {
    finnAttesteringsOppgaverForSaksbehandlerQueryKey,
    hentOppgaverForPersonQueryKey,
    hentOppgaverForSaksbehandlerQueryKey,
} from "@superhelt/api/react-query.gen";
import { useQueryClient } from "@tanstack/react-query";

/** Invaliderer cachen for en sak og tilhørende endringslogg */
export function useInvalidateOppgaveQuery() {
    const queryClient = useQueryClient();

    function invalidateOppgaveQuery(maskertPersonIdent?: string) {
        // Attesteringsoppgaver
        queryClient.invalidateQueries({ queryKey: finnAttesteringsOppgaverForSaksbehandlerQueryKey() });
        // Saksbehandlers oppgaver
        queryClient.invalidateQueries({ queryKey: hentOppgaverForSaksbehandlerQueryKey() });
        // dokumenter knyttet til saken
        if (maskertPersonIdent) {
            queryClient.invalidateQueries({
                queryKey: hentOppgaverForPersonQueryKey({ path: { maskertPersonIdent: maskertPersonIdent } }),
            });
        }
    }

    return invalidateOppgaveQuery;
}
