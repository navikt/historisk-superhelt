import {Tag} from "@navikt/ds-react";
import {Sak} from "@generated";
import {SakStatusType, SakVedtakType} from "~/routes/sak/$saksnummer/-types/sak.types";
import {ExclamationmarkTriangleIcon} from "@navikt/aksel-icons";

interface Props {
    sak: Sak
}

export default function SakStatus({sak}: Props) {
    function ferdigText(vedtak: SakVedtakType | undefined) {
        switch (vedtak) {
            case "AVSLATT":
                return "Avslått";
            case "INNVILGET":
                return "Innvilget";
            case "DELVIS_INNVILGET":
                return "Delvis innvilget";
            case "HENLAGT" :
                return "Henlagt";
        }
        return undefined;
    }

    function getAlertIcon() {
        if (sak.error.utbetalingError) {
            return <ExclamationmarkTriangleIcon title="Det oppstod en feil ved utbetaling"/>;
        }
        return undefined;
    }
    const hasError= sak.error.utbetalingError
    const status: SakStatusType = sak.status;

    const renderFerdigStatusTag = () => {
        if (sak.vedtaksResultat==="HENLAGT") {
            return <Tag variant="warning" size="small">Henlagt</Tag>
        }
        const variant = hasError ? "error" : "success";
        const icon = getAlertIcon();

        return <Tag variant={variant} size="small" icon={icon}>{ferdigText(sak.vedtaksResultat)}</Tag>
    }
//TODO oppdatere til aksel v8 og sette bedre farge
    switch (status) {
        case "FEILREGISTRERT":
            return <Tag variant="neutral-filled" size="small">Feilregistert</Tag>
        case "UNDER_BEHANDLING":
            return <Tag variant="alt2" size="small">Under behandling</Tag>
        case "TIL_ATTESTERING":
            return <Tag variant="info" size="small">Til attestering</Tag>
        case "FERDIG_ATTESTERT":
            return <Tag variant="success-moderate" size="small">Ferdig attestert</Tag>
        case "FERDIG":
            return renderFerdigStatusTag();
    }

}