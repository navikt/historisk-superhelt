import {BrevMottakerType, BrevType} from "~/routes/sak/$saksnummer/-types/brev.types";
import {hentEllerOpprettBrev} from "@generated";

export const getOrCreateBrevQueryKey = (saksnummer: string, type: BrevType, mottaker: BrevMottakerType) => {
    return ["brev", saksnummer, type, mottaker];
}
/**
 * Oppretter ett nytt brev om det ikke allerede finnes ett av denne typen for denne saken, og returnerer
 *
 * Bruker post mot backend så derfor er denne laget custom */
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
