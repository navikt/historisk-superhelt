import {LocalAlert} from "@navikt/ds-react";
import {Sak} from "@generated";

interface Props {
    sak: Sak
}

export default function SakAlert({sak}: Props) {
    const {error}= sak

    if (error.utbetalingError) {
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