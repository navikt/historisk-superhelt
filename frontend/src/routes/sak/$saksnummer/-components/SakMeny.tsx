import {ActionMenu, Button} from "@navikt/ds-react";
import {ChevronDownIcon} from "@navikt/aksel-icons";
import {Sak} from "@api";


interface SakMenyProps {
    sak: Sak
}

export default function SakMeny({sak}: SakMenyProps) {
    const {status} = sak
    const underBehandling = status === "UNDER_BEHANDLING"
    const ferdig = !underBehandling
    return <ActionMenu>
        <ActionMenu.Trigger>
            <Button
                variant="secondary"
                icon={<ChevronDownIcon aria-hidden/>}
                iconPosition="right"
                size={"small"}
            >
                Behandlingsmeny
            </Button>
        </ActionMenu.Trigger>
        <ActionMenu.Content>
            <ActionMenu.Group label={`Sak ${sak.saksnummer}`}>
                <ActionMenu.Item onSelect={console.info} disabled={ferdig}>Avvis sak</ActionMenu.Item>
                <ActionMenu.Item onSelect={console.info} disabled={ferdig}>Henlegg sak</ActionMenu.Item>
                <ActionMenu.Item onSelect={console.info} disabled={status !== "FERDIG"}>Gjenåpne sak</ActionMenu.Item>
            </ActionMenu.Group>
            <ActionMenu.Group label={"Brev"}>
                <ActionMenu.Item onSelect={console.info} disabled={ferdig}>Send informasjonsbrev</ActionMenu.Item>
                <ActionMenu.Item onSelect={console.info} disabled={ferdig}>Send innhentingsbrev</ActionMenu.Item>
            </ActionMenu.Group>
        </ActionMenu.Content>
    </ActionMenu>
}