import type {OppgaveMedSak} from "@generated";
import {BodyLong, BodyShort, Label, List, Tag, VStack} from "@navikt/ds-react";
import {useStonadsTypeNavn} from "~/common/sak/useStonadsTypeNavn";

export function OppgaveDetaljer({ oppgave }: { oppgave: OppgaveMedSak }) {
    const getStonadsTypeNavn = useStonadsTypeNavn();

    function BehandlendeSystem({ oppgave }: { oppgave: OppgaveMedSak }) {
        if (oppgave.saksnummer) {
            return (
                <Tag data-color="success" variant="outline">
                    SuperHelt
                </Tag>
            );
        }
        if (oppgave.behandlesAvApplikasjon) {
            return (
                <Tag data-color="warning" variant="strong">
                    {oppgave.behandlesAvApplikasjon}
                </Tag>
            );
        }
        if (oppgave.opprettetAv?.startsWith("jfr-infotrygd")) {
            return (
                <Tag data-color="warning" variant="strong">
                    Infotrygd
                </Tag>
            );
        }
        return (
            <Tag data-color="neutral" variant="outline">
                Ukjent
            </Tag>
        );
    }

    function Kommentar(props: { line: string }) {
        const split = props.line.split("---\\n");
        const head = split[0];
        const body = split[1]?.replaceAll("\\n", " ");
        return (
            <List.Item>
                <i>{head}</i> -- {body}
            </List.Item>
        );
    }

    return (
        <VStack gap={"space-20"}>
            <div>
                <Label textColor="subtle">Sak </Label>
                <BodyLong>
                    {getStonadsTypeNavn(oppgave.stonadsType)} - {oppgave.sakBeskrivelse}
                </BodyLong>
            </div>
            <div>
                <Label textColor="subtle">Oppgave id i gosys</Label>
                <BodyShort>{oppgave.oppgaveId}</BodyShort>
            </div>
            <div>
                <Label textColor="subtle">Behandlende system</Label>
                <BodyShort>
                    <BehandlendeSystem oppgave={oppgave} />
                </BodyShort>
            </div>
            <div>
                <Label textColor="subtle">Kommentarer</Label>
                <List>
                    {oppgave?.beskrivelse
                        ?.split("--- ")
                        .filter((line: string) => line.trim() !== "")
                        .map((line: string) => (
                            <Kommentar key={line} line={line} />
                        ))}
                </List>
            </div>
        </VStack>
    );
}
