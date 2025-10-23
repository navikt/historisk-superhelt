import {findSakerForPersonOptions, getSakBySaksnummerOptions} from "@api/@tanstack/react-query.gen";



export const finnSakerForPersonOptions = (maskertPersonId: string) => ({
    ...findSakerForPersonOptions({query: {maskertPersonId: maskertPersonId}}),
    retry: false,
});