import type { Journalpost, OppgaveMedSak, ProblemDetail, Sak } from "@generated";
import { journalforKnyttTilEksisterendeSakMutation } from "@generated/@tanstack/react-query.gen";
import { Button, VStack } from "@navikt/ds-react";
import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "@tanstack/react-router";
import { useState } from "react";
import { ErrorAlert } from "~/common/error/ErrorAlert";
import { EksisterendeSakVelger } from "./EksisterendeSakVelger";
import type { FellesData } from "./JournalforForm";

interface Props {
    oppgaveMedSak: OppgaveMedSak;
    journalPost: Journalpost;
    readOnly?: boolean;
    getCommonData: () => FellesData | undefined;
}

export function EksisterendeSakAction({ oppgaveMedSak, journalPost, readOnly, getCommonData }: Props) {
    const navigate = useNavigate();
    const [valgtSak, setValgtSak] = useState<Sak | undefined>();
    const [error, setError] = useState<string | undefined>();
    const [backendError, setBackendError] = useState<ProblemDetail | undefined>();

    const journalfor = useMutation(journalforKnyttTilEksisterendeSakMutation());

    async function handleSubmit() {
        if (readOnly) return;
        const common = getCommonData();
        if (!common) return;
        if (!valgtSak) {
            setError("Du må velge en eksisterende sak");
            return;
        }
        try {
            setBackendError(undefined);
            const saksnummer = await journalfor.mutateAsync({
                path: { journalpostId: journalPost.journalpostId },
                body: { jfrOppgaveId: oppgaveMedSak.oppgaveId, ...common, saksnummer: valgtSak.saksnummer },
            });
            await navigate({ to: "/sak/$saksnummer", params: { saksnummer } });
        } catch (e) {
            setBackendError(e as ProblemDetail);
        }
    }

    return (
        <VStack gap="space-24">
            <EksisterendeSakVelger
                maskertPersonIdent={oppgaveMedSak.maskertPersonIdent}
                valgtSaksnummer={valgtSak?.saksnummer}
                error={error}
                readOnly={readOnly}
                onVelgSak={(sak: Sak) => {
                    setValgtSak(sak);
                    setError(undefined);
                }}
            />
            <Button type="submit" disabled={readOnly} onClick={handleSubmit} loading={journalfor.isPending}>
                Journalfør på eksisterende sak
            </Button>
            {backendError && <ErrorAlert error={backendError} />}
        </VStack>
    );
}
