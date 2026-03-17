import type { Sak } from "@generated";
import type { SakStatusType } from "~/routes/sak/$saksnummer/-types/sak.types";

const ferdigStatus: Array<SakStatusType> = ["FERDIG", "FEILREGISTRERT"];

export function isSakFerdig(sak: Sak) {
    return ferdigStatus.includes(sak.status);
}
