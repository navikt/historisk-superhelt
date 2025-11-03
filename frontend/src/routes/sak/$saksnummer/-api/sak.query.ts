import {
    getKodeverkStonadTypeOptions,
    getSakBySaksnummerOptions, getSakBySaksnummerQueryKey
} from "@api/@tanstack/react-query.gen";

export const getSakOptions = (saksnummer: string) => ({
    ...getSakBySaksnummerOptions({path: {saksnummer: saksnummer}}),
    retry: false,
});

export const sakQueryKey = (saksnummer: string) => getSakBySaksnummerQueryKey({path: {saksnummer: saksnummer}})

export const getKodeverkStonadsTypeOptions = () => ({
    ...getKodeverkStonadTypeOptions(),
    staleTime: Infinity,
});

