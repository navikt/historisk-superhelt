import {getPersonByMaskertIdentOptions} from "@generated/@tanstack/react-query.gen";

export const finnPersonQuery = (maskertPersonId: string) => ({
    ...getPersonByMaskertIdentOptions({
        path: {
            maskertPersonident: maskertPersonId
        }
    })
});