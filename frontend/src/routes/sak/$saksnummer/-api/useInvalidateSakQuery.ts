import { hentEndringsloggForSakQueryKey, hentSakHistorikkForSakOptions } from "@generated/@tanstack/react-query.gen";
import { useQueryClient } from "@tanstack/react-query";
import { sakQueryKey } from "~/common/sak/sak.query";
import { apiFinnJournalposterForSakOptions } from "./journalpost.query";

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
            queryKey: apiFinnJournalposterForSakOptions(saksnummer).queryKey,
        });
        // andre dokumenter på brukeren
        queryClient.invalidateQueries({
            queryKey: apiFinnJournalposterForSakOptions(saksnummer).queryKey,
        });
        // sakshistorikk for saken
        queryClient.invalidateQueries({
            queryKey: hentSakHistorikkForSakOptions({ path: { saksnummer } }).queryKey,
        });
    }

    return invalidateSakQuery;
}
