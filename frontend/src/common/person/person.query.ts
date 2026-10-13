import { getPersonByMaskertIdentOptions } from "@superhelt/api/react-query.gen";

export const finnPersonQuery = (maskertPersonId: string) => ({
    ...getPersonByMaskertIdentOptions({
        path: {
            maskertPersonident: maskertPersonId,
        },
    }),
});
