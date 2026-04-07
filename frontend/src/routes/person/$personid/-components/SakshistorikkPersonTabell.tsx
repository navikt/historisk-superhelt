import {findSakerForPersonOptions, hentInfotrygdHistorikkForPersonOptions,} from "@generated/@tanstack/react-query.gen";
import {useSuspenseQuery} from "@tanstack/react-query";
import {isSakFerdig} from "~/common/sak/sak.utils";
import {SakshistorikkKombinertTabell} from "../../../../common/sak/historikk/SakshistorikkKombinertTabell";

interface Props {
    maskertPersonIdent: string;
}

export function SakshistorikkPersonTabell({ maskertPersonIdent }: Props) {
    const { data: saker } = useSuspenseQuery(
        findSakerForPersonOptions({ query: { maskertPersonId: maskertPersonIdent } }),
    );
    const { data: infotrygdHistorikk } = useSuspenseQuery(
        hentInfotrygdHistorikkForPersonOptions({ path: { maskertPersonIdent } }),
    );

    return (
        <SakshistorikkKombinertTabell saker={saker.filter(isSakFerdig)} infotrygdHistorikk={infotrygdHistorikk ?? []} />
    );
}
