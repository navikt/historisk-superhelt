import { Tag } from "@navikt/ds-react";
import type { SakStatusType } from "~/common/sak/sak.types";

interface Props {
    status: SakStatusType;
}

const statusConfig: Record<SakStatusType, { color: "warning" | "info" | "success" | "neutral"; label: string }> = {
    UNDER_BEHANDLING: { color: "warning", label: "Under behandling" },
    TIL_ATTESTERING: { color: "info", label: "Til attestering" },
    FERDIG_ATTESTERT: { color: "info", label: "Ferdig attestert" },
    FERDIG: { color: "success", label: "Ferdig" },
    FEILREGISTRERT: { color: "neutral", label: "Feilregistrert" },
};

export function SakStatusTag({ status }: Props) {
    const { color, label } = statusConfig[status];
    return (
        <Tag data-color={color} variant="moderate" size="small">
            {label}
        </Tag>
    );
}
