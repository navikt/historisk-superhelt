import {
    getKodeverkStonadTypeOptions,
    getSakBySaksnummerOptions
} from "@api/@tanstack/react-query.gen";

export const getSakOptions = (sakId: string) => ({
    ...getSakBySaksnummerOptions({path: {saksnummer: sakId}}),
    retry: false,
});

export const getKodeverkStonadsTypeOptions = () => ({
    ...getKodeverkStonadTypeOptions(),
    staleTime: Infinity,
});

