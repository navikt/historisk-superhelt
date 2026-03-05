import {
    getKodeverkSakStatusOptions,
    getKodeverkStonadTypeOptions,
    getKodeverkVedtaksResultatOptions,
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

export const getKodeverkSakStatusKodeOptions = () => ({
    ...getKodeverkSakStatusOptions(),
    staleTime: Infinity,
});

export const getKodeverkVedtaksResultatKodeOptions = () => ({
    ...getKodeverkVedtaksResultatOptions(),
    staleTime: Infinity,
});
