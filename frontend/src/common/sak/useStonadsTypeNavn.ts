import {useSuspenseQuery} from "@tanstack/react-query";
import {getKodeverkStonadsTypeOptions} from "~/routes/sak/$saksnummer/-api/sak.query";
import type {StonadType} from "~/common/sak/sak.types";

export function useStonadsTypeNavn() {
    const { data: stonadsTyper } = useSuspenseQuery(getKodeverkStonadsTypeOptions());
    return (type?: StonadType) => {
        if (!type){
            return ""
        }
        return stonadsTyper.find((t) => t.type === type)?.navn ?? type;
    }
}
