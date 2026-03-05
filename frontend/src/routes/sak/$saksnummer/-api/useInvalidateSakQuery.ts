import { hentEndringsloggForSakQueryKey } from "@generated/@tanstack/react-query.gen";
import { useQueryClient } from "@tanstack/react-query";
import { sakQueryKey } from "~/routes/sak/$saksnummer/-api/sak.query";

/** Invaliderer cachen for en sak og tilhørende endringslogg */
export function useInvalidateSakQuery() {
    const queryClient = useQueryClient();

    function invalidateSakQuery(saksnummer: string) {
        queryClient.invalidateQueries({ queryKey: sakQueryKey(saksnummer) });
        queryClient.invalidateQueries({
            queryKey: hentEndringsloggForSakQueryKey({ path: { saksnummer: saksnummer } }),
        });
    }

    return invalidateSakQuery;
}
