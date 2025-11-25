import {ActionMenu, Button} from "@navikt/ds-react";
import {ChevronDownIcon} from "@navikt/aksel-icons";
import {Sak} from "@generated";
import {RettighetType} from "~/routes/sak/$saksnummer/-types/sak.types";


interface SakMenyProps {
    sak: Sak
}

export default function SakMeny({sak}: SakMenyProps) {

    const hasRettighet = (rettighet: RettighetType) => {
        return sak.rettigheter.includes(rettighet)
    }
    const notSaksbehandler = !hasRettighet("SAKSBEHANDLE")

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
                <ActionMenu.Item onSelect={console.info} disabled={notSaksbehandler}>Feilregister sak</ActionMenu.Item>
                <ActionMenu.Item onSelect={console.info} disabled={notSaksbehandler}>Henlegg sak</ActionMenu.Item>
                <ActionMenu.Item onSelect={console.info} disabled={!hasRettighet("GJENAPNE")}>Gjenåpne
                    sak</ActionMenu.Item>
            </ActionMenu.Group>
            <ActionMenu.Group label={"Brev"}>
                <ActionMenu.Item onSelect={console.info} disabled={notSaksbehandler}>Send
                    informasjonsbrev</ActionMenu.Item>
                <ActionMenu.Item onSelect={console.info} disabled={notSaksbehandler}>Send
                    innhentingsbrev</ActionMenu.Item>
            </ActionMenu.Group>
        </ActionMenu.Content>
    </ActionMenu>
}