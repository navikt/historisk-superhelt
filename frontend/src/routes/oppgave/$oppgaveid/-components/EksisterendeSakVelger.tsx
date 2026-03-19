import type {Sak} from "@generated";
import {findSakerForPersonOptions} from "@generated/@tanstack/react-query.gen";
import {CheckmarkCircleIcon} from "@navikt/aksel-icons";
import {Alert, BodyShort, Box, Label, Table} from "@navikt/ds-react";
import {useSuspenseQuery} from "@tanstack/react-query";
import {useStonadsTypeNavn} from "~/common/sak/useStonadsTypeNavn";
import SakStatus from "~/routes/sak/$saksnummer/-components/SakStatus";

interface Props {
    maskertPersonIdent: string;
    valgtSaksnummer?: string;
    error?: string;
    onVelgSak: (sak: Sak) => void;
    readOnly?: boolean;
}

export function EksisterendeSakVelger({ maskertPersonIdent, valgtSaksnummer, error, onVelgSak, readOnly }: Props) {
    const { data } = useSuspenseQuery(
        findSakerForPersonOptions({ query: { maskertPersonId: maskertPersonIdent } }),
    );
    const getStonadsTypeNavn = useStonadsTypeNavn();
    const sakOppsummering = (sak: Sak) => {
        const stonadsTypeNavn = getStonadsTypeNavn(sak.type);
        return `${stonadsTypeNavn} - ${sak.beskrivelse??""}`;
    }

    const valgbareSaker = data.filter((sak) => sak.status !== "FEILREGISTRERT" );

    if (valgbareSaker.length === 0) {
        return <BodyShort>Ingen åpne saker funnet for denne personen.</BodyShort>;
    }

    return (
        <Box background={"default"}>
            {error && (
                <Alert variant="error" size="small" style={{ marginBottom: "0.5rem" }}>
                    {error}
                </Alert>
            )}
            <Label>Velg en sak:</Label>
            <Table>
                <Table.Header>
                    <Table.Row>
                        <Table.HeaderCell scope="col" />
                        <Table.HeaderCell scope="col">Saksnummer</Table.HeaderCell>
                        <Table.HeaderCell scope="col">Status</Table.HeaderCell>
                        <Table.HeaderCell scope="col"></Table.HeaderCell>
                    </Table.Row>
                </Table.Header>
                <Table.Body>
                    {valgbareSaker.map((sak) => (
                        <Table.Row
                            key={sak.saksnummer}
                            selected={sak.saksnummer === valgtSaksnummer}
                            onClick={readOnly ? undefined : () => onVelgSak(sak)}
                            style={{ cursor: readOnly ? "default" : "pointer" }}
                        >
                            <Table.DataCell>
                                {sak.saksnummer === valgtSaksnummer && (
                                    <CheckmarkCircleIcon aria-label="Valgt" fontSize="1.5rem" />
                                )}
                            </Table.DataCell>
                            <Table.HeaderCell scope="row">{sak.saksnummer}</Table.HeaderCell>
                            <Table.DataCell>
                                <SakStatus sak={sak} />
                            </Table.DataCell>
                            <Table.DataCell>{sakOppsummering(sak)}</Table.DataCell>
                        </Table.Row>
                    ))}
                </Table.Body>
            </Table>
        </Box>
    );
}
