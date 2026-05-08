import {
    getKodeverkSakStatusOptions as getKodeverkSakStatusQueryOptions,
    getKodeverkStonadTypeOptions,
    getKodeverkVedtaksResultatOptions as getKodeverkVedtaksResultatQueryOptions,
    getSakBySaksnummerOptions,
} from "@generated/@tanstack/react-query.gen";

export const getSakOptions = (saksnummer: string) => ({
    ...getSakBySaksnummerOptions({ path: { saksnummer: saksnummer } }),
});

export const sakQueryKey = (saksnummer: string) => getSakOptions(saksnummer).queryKey;

export const getKodeverkStonadsTypeOptions = () => ({
    ...getKodeverkStonadTypeOptions(),
    staleTime: Infinity,
});

export const getKodeverkSakStatusOptions = () => ({
    ...getKodeverkSakStatusQueryOptions(),
    staleTime: Infinity,
});

export const getKodeverkVedtaksResultatOptions = () => ({
    ...getKodeverkVedtaksResultatQueryOptions(),
    staleTime: Infinity,
});
