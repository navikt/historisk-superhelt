import {
    findSakerForPersonOptions,
    hentInfotrygdHistorikkForPersonOptions,
} from "@generated/@tanstack/react-query.gen";
import { useSuspenseQuery } from "@tanstack/react-query";
import { SakshistorikkKombinertTabell } from "~/common/sak/historikk/SakshistorikkKombinertTabell";
import { isSakFerdig } from "~/common/sak/sak.utils";

interface SakshistorikkJournalTabellProps {
    maskertPersonIdent: string;
}

export function SakshistorikkJournalTabell({ maskertPersonIdent }: SakshistorikkJournalTabellProps) {
    const { data, isPending, error } = useSuspenseQuery({
        ...findSakerForPersonOptions({ query: { maskertPersonId: maskertPersonIdent } }),
    });
    const { data: infotrygdHistorikk } = useSuspenseQuery(
        hentInfotrygdHistorikkForPersonOptions({ path: { maskertPersonIdent } }),
    );

    const saker = data.filter((sak) => !isSakFerdig(sak));

    return (
        <SakshistorikkKombinertTabell
            saker={saker}
            infotrygdHistorikk={infotrygdHistorikk}
            isPending={isPending}
            error={error}
            openInNewTab={true}
            size="medium"
        />
    );
}
