import type { Sak } from "@generated";
import type { SakStatusType, SakVedtakType, UtbetalingsType } from "~/common/sak/sak.types";

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
        default:
            return utbetalingsType;
    }
}

export function vedtakResultatText(vedtaksResultat: SakVedtakType) {
    switch (vedtaksResultat) {
        case "INNVILGET":
            return "Innvilget";
        case "DELVIS_INNVILGET":
            return "Delvis innvilget";
        case "AVSLATT":
            return "Avslått";
        case "HENLAGT":
            return "Henlagt";
        default:
            return undefined;
    }
}
