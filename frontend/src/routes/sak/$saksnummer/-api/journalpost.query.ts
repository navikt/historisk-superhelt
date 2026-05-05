import {
    finnJournalposterForSakEllerBrukerOptions,
    finnJournalposterForSakEllerBrukerQueryKey,
} from "@generated/@tanstack/react-query.gen";

export const apiFinnJournalposterOptions = (saksnummer: string, inkluderAndreSaker: boolean) => ({
    ...finnJournalposterForSakEllerBrukerOptions({
        path: { saksnummer: saksnummer },
        query: { inkluderAndreSaker: inkluderAndreSaker },
    }),
    queryKey: apiFinnJournalpostForSakQueryKey(saksnummer, inkluderAndreSaker),
});

export function apiFinnJournalpostForSakQueryKey(saksnummer: string, inkluderAndreSaker: boolean) {
    return finnJournalposterForSakEllerBrukerQueryKey({
        path: { saksnummer: saksnummer },
        query: { inkluderAndreSaker },
    });
}
