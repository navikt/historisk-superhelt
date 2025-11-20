import {findSakerForPersonOptions} from "@generated/@tanstack/react-query.gen";


export const finnSakerForPersonOptions = (maskertPersonId: string) => ({
    ...findSakerForPersonOptions({query: {maskertPersonId: maskertPersonId}}),
    retry: false,
});