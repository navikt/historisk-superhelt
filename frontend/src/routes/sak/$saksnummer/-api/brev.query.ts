import {BrevMottakerType, BrevType} from "~/routes/sak/$saksnummer/-types/brev.types";
import {hentEllerOpprettBrev} from "@generated";

export const getOrCreateBrevOptions= (saksnummer: string, type: BrevType, mottaker: BrevMottakerType) => ({
    queryKey: ["brev", saksnummer, type, mottaker],
    queryFn: async () => {
        const {data}= await hentEllerOpprettBrev({
            path: {saksnummer: saksnummer},
            body: {type: type, mottaker: mottaker},
        })
        return data
    },
});