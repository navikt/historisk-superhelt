import {findSakerForPersonOptions, hentOppgaverForPersonOptions} from "@generated/@tanstack/react-query.gen";
import {InfoCard, VStack} from "@navikt/ds-react";
import {useSuspenseQuery} from "@tanstack/react-query";
import {OppgaveTabell} from "~/common/oppgave/OppgaveTabell";
import {SakerTabell} from "~/common/sak/SakerTabell";
import {isSakFerdig} from "~/common/sak/sak.utils";

interface SakerTableProps {
    maskertPersonIdent: string;
}

export function OppgaverForPersonTabell({ maskertPersonIdent }: SakerTableProps) {
    const { data: saker } = useSuspenseQuery({
        ...findSakerForPersonOptions({ query: { maskertPersonId: maskertPersonIdent } }),
    });

    const { data: oppgaver, isPending } = useSuspenseQuery(
        hentOppgaverForPersonOptions({ path: { maskertPersonIdent: maskertPersonIdent } }),
    );

    const sakerMedOppgave = oppgaver.map((o) => o.saksnummer);

    const sakerUnderBehandlingUtenOppgave = saker
        .filter((sak) => !isSakFerdig(sak))
        .filter((sak) => !sakerMedOppgave.includes(sak.saksnummer));

    if (oppgaver.length === 0 && !isPending) {
        return (
            <InfoCard data-color="neutral">
                <InfoCard.Header>
                    <InfoCard.Title>Ingen oppgaver under behandling</InfoCard.Title>
                </InfoCard.Header>
                <InfoCard.Content>
                    Det er ingen oppgaver under arbeid for denne personen. Sjekk sakshistorikken for å se tidligere
                    saker.
                </InfoCard.Content>
            </InfoCard>
        );
    }

    return (
        <VStack gap={"space-16"}>
            <OppgaveTabell oppgaver={oppgaver} />
            {sakerUnderBehandlingUtenOppgave.length > 0 ? (
                <InfoCard data-color="warning">
                    <InfoCard.Header>
                        <InfoCard.Title>Personen har åpne saker uten tilknyttet oppgave</InfoCard.Title>
                    </InfoCard.Header>
                    <InfoCard.Content>
                        <SakerTabell saker={sakerUnderBehandlingUtenOppgave} />
                    </InfoCard.Content>
                </InfoCard>
            ) : null}
        </VStack>
    );
}
