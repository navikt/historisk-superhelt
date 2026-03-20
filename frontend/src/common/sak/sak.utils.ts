import type {Sak} from "@generated";
import type {SakStatusType, UtbetalingsType} from "~/common/sak/sak.types";

const ferdigStatus: Array<SakStatusType> = ["FERDIG", "FEILREGISTRERT"];

export function isSakFerdig(sak: Sak) {
    return ferdigStatus.includes(sak.status);
}

export function utbetalingText(utbetalingsType: UtbetalingsType) {
    switch (utbetalingsType) {
        case "BRUKER":
            return "Utbetaling til bruker";
        case "FORHANDSTILSAGN":
            return "Forhåndstilsagn";
        case "INGEN":
            return "Ingen utbetaling er valgt";
        default: return utbetalingsType;
    }
}
