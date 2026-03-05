import { useSuspenseQuery } from "@tanstack/react-query";
import { getKodeverkSakStatusOptions } from "~/routes/sak/$saksnummer/-api/sak.query";
import type { SakStatusType } from "~/routes/sak/$saksnummer/-types/sak.types";

export function useSakStatusNavn() {
    const { data: sakStatuser } = useSuspenseQuery(getKodeverkSakStatusOptions());
    return (status: SakStatusType) => sakStatuser.find((s) => s.status === status)?.navn ?? status;
}
