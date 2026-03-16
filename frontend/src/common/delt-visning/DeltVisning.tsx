import { Box, Button, HGrid, VStack } from "@navikt/ds-react";
import { DragVerticalIcon } from "@navikt/aksel-icons";
import { BreakpointLg } from "@navikt/ds-tokens/dist/tokens";
import { useEffect, useLayoutEffect, useRef, useState } from "react";
import styles from "./deltVisning.module.css";

const DELT_VISNING_BREDDE_KEY = "delt-visning-bredde";
const MIN_BREDDE = 0.285;
const MAX_BREDDE = 0.725;
const DEFAULT_BREDDE = 0.5;
const AKSEL_BREAKPOINT_LG = parseInt(BreakpointLg);

interface DeltVisningProps {
    children: React.ReactNode[];
}

export default function DeltVisning({ children }: DeltVisningProps) {
    return (
        <HGrid gap="space-48" columns={{ md: 1, lg: "1fr auto" }} paddingBlock={"space-8 space-0"}>
            {children}
        </HGrid>
    );
}

type DraTilstand = {
    startX: number;
    startForhold: number;
    beholderBredde: number;
    pointerId: number;
};

function begrensBreddeforhold(nesteForhold: number) {
    return Math.min(MAX_BREDDE, Math.max(MIN_BREDDE, nesteForhold));
}

function lesLagretBreddeforhold() {
    const lagret = localStorage.getItem(DELT_VISNING_BREDDE_KEY);
    if (lagret === null) {
        return DEFAULT_BREDDE;
    }

    const breddeforhold = Number(lagret);
    return Number.isFinite(breddeforhold) ? begrensBreddeforhold(breddeforhold) : DEFAULT_BREDDE;
}

function JusterbarKolonne({ children }: React.PropsWithChildren) {
    const [breddeforhold, setBreddeforhold] = useState(lesLagretBreddeforhold);
    const kolonneRef = useRef<HTMLDivElement>(null);
    const [beholderBredde, setBeholderBredde] = useState(0);
    const draTilstand = useRef<DraTilstand | null>(null);

    useLayoutEffect(() => {
        const kolonne = kolonneRef.current;
        const grid = kolonne?.parentElement;

        if (!grid) return;

        const oppdaterBeholderBredde = () => {
            setBeholderBredde(grid.getBoundingClientRect().width);
        };

        oppdaterBeholderBredde();

        const observator = new ResizeObserver(oppdaterBeholderBredde);
        observator.observe(grid);

        return () => {
            observator.disconnect();
        };
    }, []);

    useEffect(() => {
        localStorage.setItem(DELT_VISNING_BREDDE_KEY, String(breddeforhold));
    }, [breddeforhold]);

    const håndterPekerNed = (event: React.PointerEvent<HTMLButtonElement>) => {
        if (beholderBredde === 0) return; // Mulig edge case, hindrer deling på 0

        draTilstand.current = {
            startX: event.clientX,
            startForhold: breddeforhold,
            beholderBredde,
            pointerId: event.pointerId,
        };
        event.currentTarget.setPointerCapture(event.pointerId);
    };

    const håndterPekerFlytt = (event: React.PointerEvent<HTMLButtonElement>) => {
        if (!draTilstand.current) return; // Ignorerer bevegelser når vi ikke er i en dra-operasjon

        const delta = event.clientX - draTilstand.current.startX;
        const deltaForhold = delta / draTilstand.current.beholderBredde;
        setBreddeforhold(begrensBreddeforhold(draTilstand.current.startForhold - deltaForhold));
    };

    const stoppDra = (event: React.PointerEvent<HTMLButtonElement>) => {
        const aktivDraTilstand = draTilstand.current;
        if (aktivDraTilstand) {
            event.currentTarget.releasePointerCapture(aktivDraTilstand.pointerId);
        }
        draTilstand.current = null;
    };

    const kolonneBredde =
        beholderBredde > 0 ? `${Math.round(beholderBredde * breddeforhold)}px` : `${DEFAULT_BREDDE * 100}%`;

    return (
        <Box
            ref={kolonneRef}
            borderColor="neutral-subtle"
            paddingBlock="space-16 space-0"
            overflow="visible"
            style={{
                position: "relative",
                width: kolonneBredde,
                justifySelf: "start",
            }}
        >
            <Button
                className={styles.draHåndtak}
                type="button"
                variant="tertiary"
                data-color="neutral"
                onPointerDown={håndterPekerNed}
                onPointerMove={håndterPekerFlytt}
                onPointerUp={stoppDra}
                onPointerCancel={stoppDra}
                tabIndex={-1}
            >
                <div className={styles.linjer} />
                <DragVerticalIcon title="Dra for å endre bredde" fontSize="2.25rem" />
                <div className={styles.linjer} />
            </Button>
            <VStack gap="space-16">{children}</VStack>
        </Box>
    );
}

function StatiskKolonne({ children }: React.PropsWithChildren) {
    return (
        <Box borderColor="neutral-subtle" paddingBlock="space-16 space-0" asChild>
            <VStack gap="space-16">{children}</VStack>
        </Box>
    );
}

interface KolonneProps {
    children: React.ReactNode;
    justerbar?: boolean;
}

const erMindreEnnLg = () => window.innerWidth < AKSEL_BREAKPOINT_LG;

function Kolonne({ children, justerbar }: KolonneProps) {
    const [erUnderLg, setErUnderLg] = useState(erMindreEnnLg);

    useEffect(() => {
        const oppdater = () => setErUnderLg(erMindreEnnLg());

        window.addEventListener("resize", oppdater);

        return () => {
            window.removeEventListener("resize", oppdater);
        };
    }, []);

    return justerbar && !erUnderLg ? (
        <JusterbarKolonne>{children}</JusterbarKolonne>
    ) : (
        <StatiskKolonne>{children}</StatiskKolonne>
    );
}

DeltVisning.Kolonne = Kolonne;
