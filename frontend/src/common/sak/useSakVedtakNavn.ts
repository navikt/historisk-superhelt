import { useSuspenseQuery } from "@tanstack/react-query";
import { getKodeverkVedtaksResultatOptions } from "~/routes/sak/$saksnummer/-api/sak.query";
import type { SakVedtakType } from "~/routes/sak/$saksnummer/-types/sak.types";

export function useSakVedtakNavn() {
    const { data: vedtaksResultater } = useSuspenseQuery(getKodeverkVedtaksResultatOptions());
    return (vedtak: SakVedtakType | undefined | null) => {
        if (vedtak == null) {
            return "";
        }
        return vedtaksResultater.find((v) => v.vedtaksResultat === vedtak)?.navn ?? vedtak;
    };
}
