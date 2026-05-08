import { hentSakHistorikkForPersonOptions } from "@generated/@tanstack/react-query.gen";
import { useSuspenseQuery } from "@tanstack/react-query";
import { SakshistorikkKombinertTabell } from "~/common/sak/historikk/SakshistorikkKombinertTabell";
import type { TemaType } from "~/common/sak/sak.types";
import { isSakFerdig } from "~/common/sak/sak.utils";

interface Props {
    maskertPersonIdent: string;
}

export function SakshistorikkPersonTabell({ maskertPersonIdent }: Props) {
    //TODO tema som query filter optional
    const { data, isPending, error } = useSuspenseQuery({
        ...hentSakHistorikkForPersonOptions({ path: { maskertPersonIdent: maskertPersonIdent, tema: "HEL" } }),
    });

    const saker = data.saker;
    const infotrygdHistorikk = data.infotrygd;

    return <SakshistorikkKombinertTabell saker={saker.filter(isSakFerdig)} infotrygdHistorikk={infotrygdHistorikk} />;
}
