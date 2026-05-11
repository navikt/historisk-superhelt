import {
    finnJournalposterForBrukerOptions,
    finnJournalposterForSakOptions,
} from "@generated/@tanstack/react-query.gen";
import type { TemaType } from "~/common/sak/sak.types";

export const apiFinnJournalposterForSakOptions = (saksnummer: string) => ({
    ...finnJournalposterForSakOptions({ path: { saksnummer } }),
});

export const apiFinnJournalposterForBrukerOptions = (maskertPersonIdent: string, tema?: TemaType) => ({
    ...finnJournalposterForBrukerOptions({ path: { maskertPersonIdent }, query: { tema } }),
    enabled: !!maskertPersonIdent,
});
