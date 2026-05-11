import type { Sak } from "@generated";
import { hentSakHistorikkForPersonOptions } from "@generated/@tanstack/react-query.gen";
import { useSuspenseQuery } from "@tanstack/react-query";
import type { SakHistorikkResult } from "~/common/sak/historikk/sakshistorikk.types";
import type { TemaType } from "~/common/sak/sak.types";

interface SakHistorikkParams {
    maskertPersonIdent: string;
    tema?: TemaType;
    filter?: (sak: Sak) => boolean;
}

export function useSakHistorikk({ maskertPersonIdent, tema, filter = () => true }: SakHistorikkParams) {
    const { data, isPending, error } = useSuspenseQuery({
        ...hentSakHistorikkForPersonOptions({ path: { maskertPersonIdent: maskertPersonIdent }, query: { tema } }),
    });

    const saker = data.saker.filter(filter);
    const infotrygdHistorikk = data.infotrygd;

    const count = saker.length + infotrygdHistorikk.length;
    const label = count !== undefined ? `Sakshistorikk (${count})` : "Sakshistorikk";

    const result = { saker, infotrygdHistorikk, isPending, error } as SakHistorikkResult;
    return {
        result,
        count,
        label,
    };
}
