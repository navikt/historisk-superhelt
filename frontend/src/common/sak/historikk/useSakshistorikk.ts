import type { Sak } from "@generated";
import { hentSakHistorikkForPersonOptions } from "@generated/@tanstack/react-query.gen";
import { useSuspenseQuery } from "@tanstack/react-query";
import type { SakshistorikkResult } from "~/common/sak/historikk/sakshistorikk.types";
import type { TemaType } from "~/common/sak/sak.types";

interface SakshistorikkParams {
    maskertPersonIdent: string;
    tema?: TemaType;
    filter?: (sak: Sak) => boolean;
}

export function useSakshistorikk({ maskertPersonIdent, tema, filter = () => true }: SakshistorikkParams) {
    const { data, isPending, error } = useSuspenseQuery({
        ...hentSakHistorikkForPersonOptions({ path: { maskertPersonIdent: maskertPersonIdent }, query: { tema } }),
    });

    const saker = data.saker.filter(filter);
    const infotrygdHistorikk = data.infotrygd;

    const count = saker.length + infotrygdHistorikk.length;
    const label = count > 0 ? `Sakshistorikk (${count})` : "Sakshistorikk";

    const result = { saker, infotrygdHistorikk, isPending, error } as SakshistorikkResult;
    return {
        result,
        count,
        label,
    };
}
