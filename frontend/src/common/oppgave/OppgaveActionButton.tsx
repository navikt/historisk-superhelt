import { Button, HStack } from "@navikt/ds-react";
import type { OppgaveMedSak } from "@superhelt/api";
import { tilordneOppaveTilSaksbehandlerMutation } from "@superhelt/api/react-query.gen";
import { useMutation } from "@tanstack/react-query";
import { Link as RouterLink } from "@tanstack/react-router";
import { FerdigstillOppgaveDialogButton } from "~/common/oppgave/FerdigstillOppgaveDialogButton";
import { useInvalidateOppgaveQuery } from "~/common/oppgave/useInvalidateOppgaveQuery.ts";

interface Props {
    oppgave: OppgaveMedSak;
    saksbehandlerIdent: string;
}

export function OppgaveActionButton({ oppgave, saksbehandlerIdent }: Props) {
    const invalidateOppgaveQuery = useInvalidateOppgaveQuery();
    const saksnummer = oppgave.saksnummer;
    const ufordelt = !oppgave.tilordnetRessurs;
    const erTildeltSaksbehandler = !!saksbehandlerIdent && oppgave.tilordnetRessurs === saksbehandlerIdent;

    const tilOrdneOppgaveMutation = useMutation({
        ...tilordneOppaveTilSaksbehandlerMutation(),
        onSuccess: () => invalidateOppgaveQuery(oppgave.maskertPersonIdent),
    });

    const plukkOppgave = () => tilOrdneOppgaveMutation.mutate({ path: { oppgaveId: oppgave.oppgaveId } });

    const ukjentOppgave = () => {
        return <div />;
    };

    const actionButton = (to: string, title: string) => {
        if (ufordelt) {
            return (
                <Button variant="secondary" size="small" onClick={plukkOppgave}>
                    Plukk oppgave
                </Button>
            );
        }
        if (!erTildeltSaksbehandler) {
            return (
                <Button as={RouterLink} to={to} variant={"secondary-neutral"} size="small">
                    Se på
                </Button>
            );
        }

        return (
            <Button as={RouterLink} to={to} variant={"primary"} size="small">
                {title}
            </Button>
        );
    };

    const actionButtonOrDisabled = (to: string, title: string) => {
        if (!saksnummer) {
            return ukjentOppgave();
        }

        return actionButton(to, title);
    };

    const renderButton = () => {
        switch (oppgave.oppgavetype) {
            case "JFR":
                return actionButton(`/oppgave/${oppgave.oppgaveId}/journalfor`, "Journalfør");
            case "BEH_UND_VED":
            case "BEH_SAK":
                return actionButtonOrDisabled(`/sak/${saksnummer}`, "Behandle");
            case "GOD_VED":
                return actionButtonOrDisabled(`/sak/${saksnummer}/oppsummering`, "Attester");
            case "VUR":
                return <FerdigstillOppgaveDialogButton />;
        }
        return null;
    };

    return <HStack gap="space-8">{renderButton()}</HStack>;
}
