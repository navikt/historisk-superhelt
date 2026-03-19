import {useSuspenseQuery} from "@tanstack/react-query";
import type {SakStatusType} from "~/common/sak/sak.types";
import {getKodeverkSakStatusOptions} from "~/routes/sak/$saksnummer/-api/sak.query";

export function useSakStatusNavn() {
    const { data } = useSuspenseQuery(getKodeverkSakStatusOptions());
    return (status?: SakStatusType) => {
        if (!status){
            return ""
        }
        return data.find((s) => s.status === status)?.navn ?? status;
    }
}
