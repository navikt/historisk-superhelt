import {
    finnJournalposterForBrukerOptions,
    finnJournalposterForBrukerQueryKey,
    finnJournalposterForSakOptions,
    finnJournalposterForSakQueryKey,
} from "@generated/@tanstack/react-query.gen";

export const apiFinnJournalposterForSakOptions = (saksnummer: string) => ({
    ...finnJournalposterForSakOptions({ path: { saksnummer } }),
    queryKey: finnJournalposterForSakQueryKey({ path: { saksnummer } }),
});

export const apiFinnJournalposterForBrukerOptions = (saksnummer: string) => ({
    ...finnJournalposterForBrukerOptions({ path: { saksnummer } }),
    queryKey: finnJournalposterForBrukerQueryKey({ path: { saksnummer } }),
});
