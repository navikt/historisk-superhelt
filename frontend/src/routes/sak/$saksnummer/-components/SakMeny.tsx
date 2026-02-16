import {ActionMenu, Button} from "@navikt/ds-react";
import {ChevronDownIcon, EnvelopeClosedIcon, GavelIcon, PadlockUnlockedIcon, TrashIcon} from "@navikt/aksel-icons";
import {Sak} from "@generated";
import {RettighetType} from "~/routes/sak/$saksnummer/-types/sak.types";
import {Link as RouterLink} from "@tanstack/react-router";

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
            >Behandlingsmeny</Button>

        </ActionMenu.Trigger>
        <ActionMenu.Content>
            <ActionMenu.Group label={`Sak ${sak.saksnummer}`}>
                <ActionMenu.Item
                    as={RouterLink}
                    disabled={!hasRettighet("FEILREGISTERE")}
                    to={`/sak/${sak.saksnummer}/feilregistrer`}
                    icon={<TrashIcon aria-hidden/>}>Feilregistrer sak</ActionMenu.Item>
                <ActionMenu.Item
                    onSelect={console.info}
                    disabled={true}
                    icon={<GavelIcon aria-hidden/>}>Henlegg sak</ActionMenu.Item>
                <ActionMenu.Item
                    onSelect={console.info}
                    disabled={!hasRettighet("GJENAPNE")}
                    icon={<PadlockUnlockedIcon aria-hidden/>}>Gjenåpne sak</ActionMenu.Item>
            </ActionMenu.Group>
            <ActionMenu.Group label={"Brev"}>
                <ActionMenu.Item
                    as={RouterLink}
                    disabled={notSaksbehandler}
                    to={`/sak/${sak.saksnummer}/fritekstbrev`}
                    icon={<EnvelopeClosedIcon aria-hidden/>}>Fritekstbrev til bruker </ActionMenu.Item>
                <ActionMenu.Item
                    as={RouterLink}
                    disabled={true}
                    to={`/sak/${sak.saksnummer}`}
                    icon={<EnvelopeClosedIcon aria-hidden/>}>Fritekstbrev til samhandler </ActionMenu.Item>
            </ActionMenu.Group>
        </ActionMenu.Content>
    </ActionMenu>
}