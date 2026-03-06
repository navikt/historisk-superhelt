import { findSakerForPersonOptions } from "@generated/@tanstack/react-query.gen";
import { useSuspenseQuery } from "@tanstack/react-query";
import { SakerTabell } from "~/common/sak/SakerTabell";

interface SakerTableProps {
    maskertPersonIdent: string;
}

export function SakshistorikkSakTabell({ maskertPersonIdent }: SakerTableProps) {
    const { data, isPending, error } = useSuspenseQuery({
        ...findSakerForPersonOptions({ query: { maskertPersonId: maskertPersonIdent } }),
    });

    const saker = data.filter((sak) => sak.status === "FERDIG");

    return (
        <SakerTabell saker={saker} isPending={isPending} error={error} hideSaksbehandler={true} openInNewTab={true} />
    );
}
