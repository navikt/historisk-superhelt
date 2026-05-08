import { hentSakHistorikkForPersonOptions } from "@generated/@tanstack/react-query.gen";
import { useSuspenseQuery } from "@tanstack/react-query";
import { SakshistorikkKombinertTabell } from "~/common/sak/historikk/SakshistorikkKombinertTabell";
import { isSakFerdig } from "~/common/sak/sak.utils";

interface Props {
    maskertPersonIdent: string;
}

export function SakshistorikkPersonTabell({ maskertPersonIdent }: Props) {
    const { data } = useSuspenseQuery({
        ...hentSakHistorikkForPersonOptions({ path: { maskertPersonIdent: maskertPersonIdent } }),
    });

    const saker = data.saker;
    const infotrygdHistorikk = data.infotrygd;

    return <SakshistorikkKombinertTabell saker={saker.filter(isSakFerdig)} infotrygdHistorikk={infotrygdHistorikk} />;
}
