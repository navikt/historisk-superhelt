import { useSuspenseQuery } from "@tanstack/react-query";
import { getKodeverkVedtaksResultatKodeOptions } from "~/routes/sak/$saksnummer/-api/sak.query";
import type { SakVedtakType } from "~/routes/sak/$saksnummer/-types/sak.types";

export function useSakVedtakNavn() {
    const { data: vedtaksResultater } = useSuspenseQuery(getKodeverkVedtaksResultatKodeOptions());
    return (vedtak: SakVedtakType | undefined | null) =>
        vedtaksResultater.find((v) => v.vedtaksResultat === vedtak)?.navn;
}
