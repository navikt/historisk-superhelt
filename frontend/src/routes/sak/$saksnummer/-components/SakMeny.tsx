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
                <ActionMenu.Item onSelect={console.info}>Avvis</ActionMenu.Item>
                <ActionMenu.Item onSelect={console.info}>Henlegg</ActionMenu.Item>
            </ActionMenu.Group>
            <ActionMenu.Group label={"Brev"}>
                <ActionMenu.Item onSelect={console.info}>Informasjonsbrev</ActionMenu.Item>
                <ActionMenu.Item onSelect={console.info}>Innhentingsbrev</ActionMenu.Item>
            </ActionMenu.Group>
        </ActionMenu.Content>
    </ActionMenu>
}