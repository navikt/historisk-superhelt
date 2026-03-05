import { findSakerForPersonOptions } from "@generated/@tanstack/react-query.gen";
import { useSuspenseQuery } from "@tanstack/react-query";
import { SakerTabell } from "~/common/sak/SakerTabell";

interface SakshistorikkJournalTabellProps {
    maskertPersonIdent: string;
}

export function SakshistorikkJournalTabell({ maskertPersonIdent }: SakshistorikkJournalTabellProps) {
    const { data, isPending, error } = useSuspenseQuery({
        ...findSakerForPersonOptions({ query: { maskertPersonId: maskertPersonIdent } }),
    });

    const saker = data.filter((sak) => sak.status !== "FEILREGISTRERT");

    return (
        <SakerTabell saker={saker} isPending={isPending} error={error} hideSaksbehandler={true} openInNewTab={true} />
    );
}
