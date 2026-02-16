import {SakStatusType} from "~/routes/sak/$saksnummer/-types/sak.types";
import {Sak} from "@generated";

const ferdigStatus: Array<SakStatusType>= ["FERDIG","FEILREGISTRERT"]

export function isSakFerdig(sak: Sak) {
    return ferdigStatus.includes(sak.status)
}
