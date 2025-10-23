import {
    findSakerForPersonOptions,
    getKodeverkSaksTypeOptions,
    getSakBySaksnummerOptions
} from "@api/@tanstack/react-query.gen";

export const getSakOptions = (sakId: string) => ({
    ...getSakBySaksnummerOptions({path: {saksnummer: sakId}}),
    retry: false,
});

export const getKodeverkSakTypeOptions = () => ({
    ...getKodeverkSaksTypeOptions(),
    staleTime: Infinity,
});

