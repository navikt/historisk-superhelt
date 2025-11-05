import {ActionMenu, Button} from "@navikt/ds-react";
import {ChevronDownIcon} from "@navikt/aksel-icons";


export default function SakMeny(){
    return <ActionMenu>
        <ActionMenu.Trigger>
            <Button
                variant="secondary-neutral"
                icon={<ChevronDownIcon aria-hidden />}
                iconPosition="right"
                size={"small"}
            >
                Behandlingsmeny
            </Button>
        </ActionMenu.Trigger>
        <ActionMenu.Content>
            <ActionMenu.Group label={"Saksbehandling"}>
                <ActionMenu.Item onSelect={console.info}>Avvis sak</ActionMenu.Item>
                <ActionMenu.Item onSelect={console.info}>Henlegg sak</ActionMenu.Item>
                <ActionMenu.Item onSelect={console.info}>Gjenåpne sak</ActionMenu.Item>
            </ActionMenu.Group>
            <ActionMenu.Group label={"Brev"}>
                <ActionMenu.Item onSelect={console.info}>Send informasjonsbrev</ActionMenu.Item>
                <ActionMenu.Item onSelect={console.info}>Send innhentingsbrev</ActionMenu.Item>
            </ActionMenu.Group>
        </ActionMenu.Content>
    </ActionMenu>
}