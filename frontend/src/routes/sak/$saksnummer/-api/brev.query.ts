import {BrevMottakerType, BrevType} from "~/routes/sak/$saksnummer/-types/brev.types";
import {hentEllerOpprettBrev} from "@generated";

export const getOrCreateBrevQueryKey = (saksnummer: string, type: "VEDTAKSBREV" | "INFORMASJONSBREV" | "INNHENTINGSBREV", mottaker: "BRUKER" | "SAMHANDLER") => {
    return ["brev", saksnummer, type, mottaker];
}
export const getOrCreateBrevOptions= (saksnummer: string, type: BrevType, mottaker: BrevMottakerType) => ({
    queryKey: getOrCreateBrevQueryKey(saksnummer, type, mottaker),
    queryFn: async () => {
        const {data}= await hentEllerOpprettBrev({
            path: {saksnummer: saksnummer},
            body: {type: type, mottaker: mottaker},
        })
        return data
    },
});
