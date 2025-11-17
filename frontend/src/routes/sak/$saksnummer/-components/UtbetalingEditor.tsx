import {Radio, RadioGroup, VStack} from '@navikt/ds-react'
import {useState} from "react";
import {Sak, UtbetalingRequestDto} from "@generated";
import {UtbetalingsType} from "~/routes/sak/$saksnummer/-types/sak.types";
import {NumericInput} from "~/components/NumericInput";
import {useMutation} from "@tanstack/react-query";
import {oppdaterUtbetalingMutation} from "@generated/@tanstack/react-query.gen";


interface Props {
    sak: Sak,
}

export default function UtbetalingEditor({sak}: Props) {
    const saksnummer = sak.saksnummer


    const oppdaterUtbetaling = useMutation({
        ...oppdaterUtbetalingMutation()
    })


    const initialState = (): UtbetalingRequestDto => {
        const utbetalingsType = sak.utbetalingsType;
        let belop: number | undefined = undefined;
        switch (utbetalingsType) {
            case "BRUKER":
                belop = sak.utbetaling?.belop
                break;
            case "FORHANDSTILSAGN":
                belop = sak.forhandstilsagn?.belop
                break;
            case "INGEN":
                belop = undefined
                break;

        }
        return {utbetalingsType: utbetalingsType, belop: belop}
    };

    const [utbetalingData, setUtbetalingData] = useState<UtbetalingRequestDto>(initialState())

    function lagreUtbetaling(data: UtbetalingRequestDto = utbetalingData) {
        oppdaterUtbetaling.mutate({
            path: {
                saksnummer: saksnummer
            },
            body: data
        })
    }

    const changeUtbetalingsType = (type: UtbetalingsType) => {
        const updatedData = {...utbetalingData, utbetalingsType: type};
        setUtbetalingData(updatedData);
        lagreUtbetaling(updatedData)
    }

    const changeBelop = (belop: number | undefined) => {
        const updatedData = {...utbetalingData, belop: belop};
        setUtbetalingData(updatedData);
    }

    return (
        <VStack style={{flex: 1}}>
            <RadioGroup
                legend="Utbetaling"
                value={utbetalingData.utbetalingsType}
                onChange={changeUtbetalingsType}
            >
                <Radio value="BRUKER">Utbetaling til bruker</Radio>
                <Radio value="FORHANDSTILSAGN">Forhåndstilsagn (faktura kommer)</Radio>

            </RadioGroup>
            <NumericInput
                value={utbetalingData.belop}
                onChange={belop => changeBelop(belop)}
                label="Beløp (kr)"
                onBlur={() => lagreUtbetaling()}
            />
        </VStack>

    )
}
