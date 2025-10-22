import {findSakerForPersonOptions, getSakBySaksnummerOptions} from "@api/@tanstack/react-query.gen";

export const getSakOptions = (sakId: string) => ({
    ...getSakBySaksnummerOptions({path: {saksnummer: sakId}}),
    retry: false,
});

