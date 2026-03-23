import type { Sak } from "@generated";
import { getSakStatusOptions } from "@generated/@tanstack/react-query.gen";
import { ExclamationmarkTriangleIcon } from "@navikt/aksel-icons";
import { Tag } from "@navikt/ds-react";
import { useSuspenseQuery } from "@tanstack/react-query";
import type { SakStatusType } from "~/common/sak/sak.types";
import { vedtakResultatText } from "~/common/sak/sak.utils";

interface Props {
    sak: Sak;
}

export default function SakStatus({ sak }: Props) {
    const { data: sakStatus } = useSuspenseQuery(getSakStatusOptions({ path: { saksnummer: sak.saksnummer } }));
    const hasError = sakStatus.aggregertStatus === "FEILET";

    function getAlertIcon() {
        if (hasError) {
            return <ExclamationmarkTriangleIcon title="Det er noe feil med saken" />;
        }
    }

    const status: SakStatusType = sak.status;

    const renderFerdigStatusTag = () => {
        if (sak.vedtaksResultat === "HENLAGT") {
            return (
                <Tag data-color="meta-purple" variant="outline" size="small">
                    Henlagt
                </Tag>
            );
        }
        const variant = hasError ? "error" : "success";
        const icon = getAlertIcon();

        return (
            <Tag variant={variant} size="small" icon={icon}>
                {vedtakResultatText(sak.vedtaksResultat)}
            </Tag>
        );
    };
    switch (status) {
        case "FEILREGISTRERT":
            return (
                <Tag data-color="neutral" variant="strong" size="small">
                    Feilregistert
                </Tag>
            );
        case "UNDER_BEHANDLING":
            return (
                <Tag data-color="warning" variant="outline" size="small">
                    Under behandling
                </Tag>
            );
        case "TIL_ATTESTERING":
            return (
                <Tag data-color="info" variant="outline" size="small">
                    Til attestering
                </Tag>
            );
        case "FERDIG_ATTESTERT":
            return (
                <Tag data-color="success" variant="moderate" size="small">
                    Ferdig attestert
                </Tag>
            );
        case "FERDIG":
            return renderFerdigStatusTag();
    }
}
