import type {Sak} from "@generated";
import {getSakStatusOptions} from "@generated/@tanstack/react-query.gen";
import {LocalAlert} from "@navikt/ds-react";
import {useSuspenseQuery} from "@tanstack/react-query";

interface Props {
    sak: Sak
}

export default function SakAlert({sak}: Props) {
    const {data: sakStatus} = useSuspenseQuery(getSakStatusOptions({path: {saksnummer: sak.saksnummer}}))

    if (sakStatus.utbetalingStatus === "FEILET") {
        return <LocalAlert status="error">
            <LocalAlert.Header>
                <LocalAlert.Title>
                    Utbetaling feilet
                </LocalAlert.Title>
            </LocalAlert.Header>
            <LocalAlert.Content>
                Det oppstod en feil ved utbetaling av vedtaket. Prøv på nytt eller kontakt support for å løse problemet.
            </LocalAlert.Content>
        </LocalAlert>
    }
}