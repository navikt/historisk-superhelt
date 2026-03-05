import {
    getKodeverkSakStatusOptions as getKodeverkSakStatusQueryOptions,
    getKodeverkStonadTypeOptions,
    getKodeverkVedtaksResultatOptions as getKodeverkVedtaksResultatQueryOptions,
    getSakBySaksnummerOptions,
    getSakBySaksnummerQueryKey,
} from "@generated/@tanstack/react-query.gen";

export const getSakOptions = (saksnummer: string) => ({
    ...getSakBySaksnummerOptions({ path: { saksnummer: saksnummer } }),
    retry: false,
});

export const sakQueryKey = (saksnummer: string) => getSakBySaksnummerQueryKey({ path: { saksnummer: saksnummer } });

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
