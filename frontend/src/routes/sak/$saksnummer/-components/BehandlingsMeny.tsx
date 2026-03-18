import type { Sak } from "@generated";
import {
    ArrowUndoIcon,
    ChevronDownIcon,
    EnvelopeClosedIcon,
    GavelIcon,
    PadlockUnlockedIcon,
    TrashIcon,
} from "@navikt/aksel-icons";
import { ActionMenu, Button } from "@navikt/ds-react";
import { useState } from "react";
import type { RettighetType } from "~/routes/sak/$saksnummer/-types/sak.types";
import { Feilregistrer } from "./Feilregistrer";
import { FritekstBrev } from "./Fritekstbrev";
import { Gjenapne } from "./Gjenapne";
import { Henlegg } from "./Henlegg";
import { Tilbakestill } from "./Tilbakestill";

interface SakMenyProps {
    sak: Sak;
}

export default function BehandlingsMeny({ sak }: SakMenyProps) {
    const harRettighet = (rettighet: RettighetType) => sak.rettigheter.includes(rettighet);
    const [openFeilregistrer, setOpenFeilregistrer] = useState(false);
    const [openHenlegg, setOpenHenlegg] = useState(false);
    const [openGjenapne, setOpenGjenapne] = useState(false);
    const [openTilbakestill, setOpenTilbakestill] = useState(false);
    const [openFritekstbrev, setOpenFritekstbrev] = useState(false);

    return (
        <>
            <ActionMenu>
                <ActionMenu.Trigger>
                    <Button
                        variant="secondary"
                        icon={<ChevronDownIcon aria-hidden />}
                        iconPosition="right"
                        size="medium"
                    >
                        Behandlingsmeny
                    </Button>
                </ActionMenu.Trigger>
                <ActionMenu.Content>
                    <ActionMenu.Group label={`Sak ${sak.saksnummer}`}>
                        {harRettighet("TILBAKESTILL_GJENAPNING") ? (
                            <ActionMenu.Item
                                onSelect={() => setOpenTilbakestill(true)}
                                icon={<ArrowUndoIcon aria-hidden />}
                                aria-haspopup="dialog"
                            >
                                Angre gjenåpning
                            </ActionMenu.Item>
                        ) : (
                            <ActionMenu.Item
                                disabled={!harRettighet("FEILREGISTERE")}
                                onSelect={() => setOpenFeilregistrer(true)}
                                icon={<TrashIcon aria-hidden />}
                                aria-haspopup="dialog"
                            >
                                Feilregistrer sak
                            </ActionMenu.Item>
                        )}
                        <ActionMenu.Item
                            onSelect={() => setOpenHenlegg(true)}
                            disabled={!harRettighet("HENLEGGE")}
                            icon={<GavelIcon aria-hidden />}
                            aria-haspopup="dialog"
                        >
                            Henlegg sak
                        </ActionMenu.Item>
                        <ActionMenu.Item
                            onSelect={() => setOpenGjenapne(true)}
                            disabled={!harRettighet("GJENAPNE")}
                            icon={<PadlockUnlockedIcon aria-hidden />}
                            aria-haspopup="dialog"
                        >
                            Gjenåpne sak
                        </ActionMenu.Item>
                    </ActionMenu.Group>
                    <ActionMenu.Group label={"Brev"}>
                        <ActionMenu.Item
                            onSelect={() => setOpenFritekstbrev(true)}
                            disabled={!harRettighet("SAKSBEHANDLE")}
                            icon={<EnvelopeClosedIcon aria-hidden />}
                            aria-haspopup="dialog"
                        >
                            Fritekstbrev til bruker{" "}
                        </ActionMenu.Item>
                        <ActionMenu.Item disabled={true} icon={<EnvelopeClosedIcon aria-hidden />}>
                            Fritekstbrev til samhandler{" "}
                        </ActionMenu.Item>
                    </ActionMenu.Group>
                </ActionMenu.Content>
            </ActionMenu>

            <Feilregistrer open={openFeilregistrer} onOpenChange={setOpenFeilregistrer} />
            <Henlegg open={openHenlegg} onOpenChange={setOpenHenlegg} />
            <Gjenapne open={openGjenapne} onOpenChange={setOpenGjenapne} />
            <Tilbakestill open={openTilbakestill} onOpenChange={setOpenTilbakestill} />
            <FritekstBrev open={openFritekstbrev} onOpenChange={setOpenFritekstbrev} />
        </>
    );
}
