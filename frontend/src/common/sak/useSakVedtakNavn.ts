import { useSuspenseQuery } from "@tanstack/react-query";
import type { SakVedtakType } from "~/common/sak/sak.types";
import { getKodeverkVedtaksResultatOptions } from "./sak.query";

export function useSakVedtakNavn() {
    const { data: vedtaksResultater } = useSuspenseQuery(getKodeverkVedtaksResultatOptions());
    return (vedtak: SakVedtakType | undefined | null) => {
        if (vedtak == null) {
            return "";
        }
        return vedtaksResultater.find((v) => v.vedtaksResultat === vedtak)?.navn ?? vedtak;
    };
}
