import { hentEndringsloggForSakQueryKey } from "@generated/@tanstack/react-query.gen";
import { useQueryClient } from "@tanstack/react-query";
import { apiFinnJournalpostForSakQueryKey } from "~/routes/sak/$saksnummer/-api/journalpost.query";
import { sakQueryKey } from "~/routes/sak/$saksnummer/-api/sak.query";

/** Invaliderer cachen for en sak og tilhørende endringslogg */
export function useInvalidateSakQuery() {
    const queryClient = useQueryClient();

    function invalidateSakQuery(saksnummer: string) {
        // Sak
        queryClient.invalidateQueries({ queryKey: sakQueryKey(saksnummer) });
        // Endringsloggen
        queryClient.invalidateQueries({
            queryKey: hentEndringsloggForSakQueryKey({ path: { saksnummer: saksnummer } }),
        });
        // dokumenter knyttet til saken
        queryClient.invalidateQueries({
            queryKey: apiFinnJournalpostForSakQueryKey(saksnummer),
        });
    }

    return invalidateSakQuery;
}
