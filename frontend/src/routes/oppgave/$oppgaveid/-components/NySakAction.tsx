import type {Journalpost, OppgaveMedSak, ProblemDetail} from "@generated";
import {journalforNySakMutation} from "@generated/@tanstack/react-query.gen";
import {Button, VStack} from "@navikt/ds-react";
import {useMutation} from "@tanstack/react-query";
import {useNavigate} from "@tanstack/react-router";
import {useState} from "react";
import {ErrorAlert} from "~/common/error/ErrorAlert";
import type {StonadType} from "~/common/sak/sak.types";
import type {FellesData} from "./JournalforForm";
import {StonadsTypeVelger} from "./StonadsTypeVelger";

interface Props {
    oppgaveMedSak: OppgaveMedSak;
    journalPost: Journalpost;
    defaultStonadstype?: StonadType;
    readOnly?: boolean;
    getCommonData: () => FellesData | undefined;
}

export function NySakAction({ oppgaveMedSak, journalPost, defaultStonadstype, readOnly, getCommonData }: Props) {
    const navigate = useNavigate();
    const [stonadstype, setStonadstype] = useState<StonadType | undefined>(defaultStonadstype);
    const [error, setError] = useState<string | undefined>();
    const [backendError, setBackendError] = useState<ProblemDetail | undefined>();

    const journalfor = useMutation(journalforNySakMutation());

    async function handleSubmit() {
        if (readOnly) return;
        const common = getCommonData();
        if (!common) return;
        if (!stonadstype) {
            setError("Stønadstype er påkrevd");
            return;
        }
        try {
            setBackendError(undefined);
            const saksnummer = await journalfor.mutateAsync({
                path: { journalpostId: journalPost.journalpostId },
                body: { jfrOppgaveId: oppgaveMedSak.oppgaveId, ...common, stonadsType: stonadstype },
            });
            await navigate({ to: "/sak/$saksnummer", params: { saksnummer } });
        } catch (e) {
            setBackendError(e as ProblemDetail);
        }
    }

    return (
        <VStack gap="space-24">
            <StonadsTypeVelger
                name="stonadstype"
                value={stonadstype}
                error={error}
                readOnly={readOnly}
                onChange={(v) => {
                    setStonadstype(v);
                    setError(undefined);
                }}
            />
            <Button type="submit" disabled={readOnly} onClick={handleSubmit} loading={journalfor.status==="pending"}>
                Journalfør og start behandling
            </Button>
            {backendError && <ErrorAlert error={backendError} />}
        </VStack>
    );
}
