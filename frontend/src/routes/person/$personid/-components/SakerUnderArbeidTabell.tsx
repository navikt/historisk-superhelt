import {useSuspenseQuery} from "@tanstack/react-query";
import {findSakerForPersonOptions} from "@generated/@tanstack/react-query.gen";
import {SakerTabell} from "~/common/sak/SakerTabell";
import {InfoCard} from "@navikt/ds-react";
import {isSakFerdig} from "~/routes/person/$personid/-components/sak.utils";


interface SakerTableProps {
    maskertPersonIdent: string
}

export function SakerUnderArbeidTabell({maskertPersonIdent}: SakerTableProps) {

    const {data, isPending, error} = useSuspenseQuery(({
        ...findSakerForPersonOptions({query: {maskertPersonId: maskertPersonIdent}}),
        retry: false,
    }))

    const saker = data.filter(sak =>!isSakFerdig(sak))
    if (saker.length === 0 && !isPending) {
        return <InfoCard data-color="neutral">
            <InfoCard.Header>
                <InfoCard.Title>Ingen saker under behandling</InfoCard.Title>
            </InfoCard.Header>
            <InfoCard.Content>
                Det er ingen saker under arbeid for denne personen. Sjekk sakshistorikken for å se tidligere saker.
            </InfoCard.Content>
        </InfoCard>
    }

    return <SakerTabell saker={saker} isPending={isPending} error={error}/>

}

