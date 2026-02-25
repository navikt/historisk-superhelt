import {useSuspenseQuery} from "@tanstack/react-query";
import {getKodeverkStonadsTypeOptions} from "~/routes/sak/$saksnummer/-api/sak.query";
import {StonadType} from "~/routes/sak/$saksnummer/-types/sak.types";

export function useStonadsTypeNavn() {
    const {data: stonadsTyper} = useSuspenseQuery(getKodeverkStonadsTypeOptions())
    return (type: StonadType) => stonadsTyper.find(t => t.type === type)?.navn ?? type
}