import {
    finnJournalposterForSakEllerBrukerOptions,
    finnJournalposterForSakEllerBrukerQueryKey,
} from "@generated/@tanstack/react-query.gen";

export const apiFinnJournalposterOptions = (saksnummer: string, inkluderAndreSaker: boolean) => ({
    ...finnJournalposterForSakEllerBrukerOptions({
        path: {saksnummer: saksnummer},
        query: {inkluderAndreSaker: inkluderAndreSaker}
    }),
    queryKey: apiFinnJournalpostForSakQueryKey(saksnummer),
});

export function apiFinnJournalpostForSakQueryKey(saksnummer: string) {
    return finnJournalposterForSakEllerBrukerQueryKey({path: {saksnummer: saksnummer}});
}
