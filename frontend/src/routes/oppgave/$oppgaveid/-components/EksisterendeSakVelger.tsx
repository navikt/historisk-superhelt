import type {Sak} from "@generated";
import {findSakerForPersonOptions} from "@generated/@tanstack/react-query.gen";
import {CheckmarkCircleIcon} from "@navikt/aksel-icons";
import {Alert, BodyShort, Table} from "@navikt/ds-react";
import {useSuspenseQuery} from "@tanstack/react-query";
import {isoTilLokal} from "~/common/dato.utils";
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

    const valgbareSaker = data.filter((sak) => sak.status !== "FEILREGISTRERT" );

    if (valgbareSaker.length === 0) {
        return <BodyShort>Ingen åpne saker funnet for denne personen.</BodyShort>;
    }

    return (
        <div>
            {error && (
                <Alert variant="error" size="small" style={{ marginBottom: "0.5rem" }}>
                    {error}
                </Alert>
            )}
            <Table>
                <Table.Header>
                    <Table.Row>
                        <Table.HeaderCell scope="col">Saksnummer</Table.HeaderCell>
                        <Table.HeaderCell scope="col">Type</Table.HeaderCell>
                        <Table.HeaderCell scope="col">Status</Table.HeaderCell>
                        <Table.HeaderCell scope="col">Opprettet</Table.HeaderCell>
                        <Table.HeaderCell scope="col" />
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
                            <Table.HeaderCell scope="row">{sak.saksnummer}</Table.HeaderCell>
                            <Table.DataCell>{getStonadsTypeNavn(sak.type)}</Table.DataCell>
                            <Table.DataCell>
                                <SakStatus sak={sak} />
                            </Table.DataCell>
                            <Table.DataCell>{isoTilLokal(sak.opprettetDato)}</Table.DataCell>
                            <Table.DataCell>
                                {sak.saksnummer === valgtSaksnummer && (
                                    <CheckmarkCircleIcon aria-label="Valgt" fontSize="1.5rem" />
                                )}
                            </Table.DataCell>
                        </Table.Row>
                    ))}
                </Table.Body>
            </Table>
        </div>
    );
}
