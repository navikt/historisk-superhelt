import { hentSakHistorikkForPersonOptions } from "@generated/@tanstack/react-query.gen";
import { useSuspenseQuery } from "@tanstack/react-query";
import { SakshistorikkKombinertTabell } from "~/common/sak/historikk/SakshistorikkKombinertTabell";
import type { TemaType } from "~/common/sak/sak.types";
import { isSakFerdig } from "~/common/sak/sak.utils";

interface SakshistorikkJournalTabellProps {
    maskertPersonIdent: string;
    tema?: TemaType;
}

export function SakshistorikkJournalTabell({ maskertPersonIdent, tema }: SakshistorikkJournalTabellProps) {
    const { data, isPending, error } = useSuspenseQuery({
        ...hentSakHistorikkForPersonOptions({ path: { maskertPersonIdent: maskertPersonIdent }, query: { tema } }),
    });

    const saker = data.saker.filter((sak) => !isSakFerdig(sak));
    const infotrygdHistorikk = data.infotrygd;

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
