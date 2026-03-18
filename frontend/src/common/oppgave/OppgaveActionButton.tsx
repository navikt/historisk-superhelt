import type {OppgaveMedSak} from "@generated";
import {Button, HStack} from "@navikt/ds-react";
import {Link as RouterLink} from "@tanstack/react-router";
import {FerdigstillOppgaveDialogButton} from "~/common/oppgave/FerdigstillOppgaveDialogButton";

interface Props {
    oppgave: OppgaveMedSak;
    saksbehandlerIdent: string;
}

export function OppgaveActionButton({ oppgave, saksbehandlerIdent }: Props) {

    const saksnummer = oppgave.saksnummer;
    const tildeltOppgave = !!saksbehandlerIdent && oppgave.tilordnetRessurs === saksbehandlerIdent;

    const ukjentOppgave = () => {
        return <div />;
    };

    const actionButton = (to: string, title: string) => {
        return (
            <Button as={RouterLink} to={to} variant={tildeltOppgave ? "primary" : "secondary"} size="xsmall">
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
