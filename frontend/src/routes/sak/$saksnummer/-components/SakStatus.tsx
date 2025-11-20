import {Tag} from "@navikt/ds-react";
import {Sak} from "@generated";
import {SakVedtakType} from "~/routes/sak/$saksnummer/-types/sak.types";

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
            case "AVVIST":
                return "Avvisst";
            case "HENLAGT" :
                return "Henlagt";

        }
        return undefined;
    }

    switch (sak.status) {
        case "UNDER_BEHANDLING":
            return <Tag variant="warning" size="small">Under behandling</Tag>
        case "TIL_ATTESTERING":
            return <Tag variant="info" size="small">Til attestering</Tag>
        case "FERDIG":
            return <Tag variant="success" size="small">{ferdigText(sak.vedtak)}</Tag>
    }

}