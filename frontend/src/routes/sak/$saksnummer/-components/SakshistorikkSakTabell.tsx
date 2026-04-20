import {findSakerForPersonOptions, hentInfotrygdHistorikkForPersonOptions} from "@generated/@tanstack/react-query.gen";
import {useSuspenseQuery} from "@tanstack/react-query";
import {SakshistorikkKombinertTabell} from "~/common/sak/historikk/SakshistorikkKombinertTabell";

interface SakerTableProps {
    maskertPersonIdent: string;
}

export function SakshistorikkSakTabell({maskertPersonIdent}: SakerTableProps) {
    const {data, isPending, error} = useSuspenseQuery({
        ...findSakerForPersonOptions({query: {maskertPersonId: maskertPersonIdent}}),
    });
    const {data: infotrygdHistorikk} = useSuspenseQuery(
        hentInfotrygdHistorikkForPersonOptions({path: {maskertPersonIdent}}),
    );


    const saker = data
        .filter((sak) => sak.status === "FERDIG");

    return (
        <SakshistorikkKombinertTabell saker={saker} infotrygdHistorikk={infotrygdHistorikk} isPending={isPending}
                                      error={error} hideSaksbehandler={true} openInNewTab={true}/>
    );
}
