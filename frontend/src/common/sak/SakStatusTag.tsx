import { Tag } from "@navikt/ds-react";
import type { SakStatusType } from "~/common/sak/sak.types";
import { useSakStatusNavn } from "~/common/sak/useSakStatusNavn";

interface Props {
    status: SakStatusType;
}

const statusConfig: Record<SakStatusType, { color: "warning" | "info" | "success" | "neutral" }> = {
    UNDER_BEHANDLING: { color: "warning" },
    TIL_ATTESTERING: { color: "info" },
    FERDIG_ATTESTERT: { color: "info" },
    FERDIG: { color: "success" },
    FEILREGISTRERT: { color: "neutral" },
};

export function SakStatusTag({ status }: Props) {
    const label = useSakStatusNavn(status);
    const { color } = statusConfig[status];
    return (
        <Tag data-color={color} variant="moderate" size="small">
            {label}
        </Tag>
    );
}
