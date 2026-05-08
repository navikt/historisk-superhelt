import { hentSakHistorikkForSakOptions } from "@generated/@tanstack/react-query.gen";
import { useSuspenseQuery } from "@tanstack/react-query";
import { SakshistorikkKombinertTabell } from "~/common/sak/historikk/SakshistorikkKombinertTabell";

interface SakerTableProps {
    saksnummer: string;
}

export function SakshistorikkSakTabell({ saksnummer }: SakerTableProps) {
    const { data, isPending, error } = useSuspenseQuery({
        ...hentSakHistorikkForSakOptions({ path: { saksnummer } }),
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
