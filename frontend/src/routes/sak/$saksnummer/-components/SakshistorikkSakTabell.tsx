import { hentSakHistorikkForPersonOptions } from "@generated/@tanstack/react-query.gen";
import { useSuspenseQuery } from "@tanstack/react-query";
import { SakshistorikkKombinertTabell } from "~/common/sak/historikk/SakshistorikkKombinertTabell";
import type { TemaType } from "~/common/sak/sak.types";

interface SakerTableProps {
    maskertPersonIdent: string;
    tema: TemaType;
}

export function SakshistorikkSakTabell({ maskertPersonIdent, tema }: SakerTableProps) {
    const { data, isPending, error } = useSuspenseQuery({
        ...hentSakHistorikkForPersonOptions({ path: { maskertPersonIdent: maskertPersonIdent, tema: tema } }),
    });

    const saker = data.saker.filter((sak) => sak.status === "FERDIG");
    const infotrygdHistorikk = data.infotrygd;

    return (
        <SakshistorikkKombinertTabell
            saker={saker}
            size="medium"
            infotrygdHistorikk={infotrygdHistorikk}
            isPending={isPending}
            error={error}
            openInNewTab
        />
    );
}
