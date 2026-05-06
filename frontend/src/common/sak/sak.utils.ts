import type { Sak } from "@generated";
import type { SakStatusType, SakVedtakType, UtbetalingsType } from "~/common/sak/sak.types";

const ferdigStatus: Array<SakStatusType> = ["FERDIG", "FEILREGISTRERT"];

const avslattVedtaksResultat: Array<SakVedtakType> = ["AVSLATT", "HENLAGT"];

export function isSakFerdig(sak: Sak) {
    return ferdigStatus.includes(sak.status);
}

export function vedtakAvslatt(sak: Sak) {
    return avslattVedtaksResultat.includes(sak.vedtaksResultat);
}

export function utbetalingText(utbetalingsType: UtbetalingsType) {
    switch (utbetalingsType) {
        case "BRUKER":
            return "Utbetaling til bruker";
        case "FORHANDSTILSAGN":
            return "Forhåndstilsagn";
        default:
            return "Ingen utbetaling er valgt";
    }
}
