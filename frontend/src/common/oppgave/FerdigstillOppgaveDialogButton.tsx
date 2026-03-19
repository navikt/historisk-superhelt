import {Button, Dialog} from "@navikt/ds-react";


export function FerdigstillOppgaveDialogButton() {
    return <Dialog>
        <Dialog.Trigger>
            <Button variant="tertiary" size="xsmall">Ferdigstill oppgave</Button>
        </Dialog.Trigger>
        <Dialog.Popup>
            <Dialog.Header>
                <Dialog.Title>Ferdigstill oppgave</Dialog.Title>
                <Dialog.Description>Oppgaven må ferdigstilles i Gosys</Dialog.Description>
            </Dialog.Header>
            <Dialog.Body>
            </Dialog.Body>
            <Dialog.Footer>
                <Dialog.CloseTrigger>
                    <Button>Lukk</Button>
                </Dialog.CloseTrigger>
            </Dialog.Footer>
        </Dialog.Popup>
    </Dialog>
}