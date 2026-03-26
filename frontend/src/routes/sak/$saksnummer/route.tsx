import { FilePdfIcon, TasklistIcon } from "@navikt/aksel-icons";
import { Box, HStack, Tabs } from "@navikt/ds-react";
import { useSuspenseQuery } from "@tanstack/react-query";
import { createFileRoute, Outlet } from "@tanstack/react-router";
import { useEffect } from "react";
import DeltVisning from "~/common/delt-visning/DeltVisning";
import { ErrorAlert } from "~/common/error/ErrorAlert";
import { RfcErrorBoundary } from "~/common/error/RfcErrorBoundary";
import { PersonHeader } from "~/common/person/PersonHeader";
import { finnPersonQuery } from "~/common/person/person.query";
import { ProcessMenu } from "~/common/process-menu/ProcessMenu";
import { StepType } from "~/common/process-menu/StepType";
import type { TilstandStatusType } from "~/common/sak/sak.types";
import { isSakFerdig } from "~/common/sak/sak.utils";
import { kortNavn, kortSaksnummer } from "~/common/string.utils";
import DokumentViewer from "~/routes/sak/$saksnummer/-components/dokumenter/DokumentViewer";
import SakAlert from "~/routes/sak/$saksnummer/-components/SakAlerts";
import SakOppsummering from "~/routes/sak/$saksnummer/-components/SakOppsummering";
import { SakshistorikkSakTabell } from "~/routes/sak/$saksnummer/-components/SakshistorikkSakTabell";
import { getSakOptions } from "./-api/sak.query";
import BehandlingsMeny from "./-components/BehandlingsMeny";

export const Route = createFileRoute("/sak/$saksnummer")({
    component: SakLayout,
    loader: ({ params: { saksnummer }, context }) => {
        context.queryClient.ensureQueryData(getSakOptions(saksnummer));
    },
    errorComponent: ({ error }) => {
        return <ErrorAlert error={error} />;
    },
});

function SakLayout() {
    const { saksnummer } = Route.useParams();
    const { data: sak } = useSuspenseQuery(getSakOptions(saksnummer));
    const { data: person } = useSuspenseQuery(finnPersonQuery(sak.maskertPersonIdent));

    useEffect(() => {
        document.title = `${kortSaksnummer(sak.saksnummer)} – ${kortNavn(person.navn)}`;
        return () => {
            document.title = "Superhelt";
        };
    }, [sak.saksnummer, person.navn]);

    const isFerdig = isSakFerdig(sak);

    const calculateStepType = (tilstandResultat: TilstandStatusType): StepType => {
        if (isFerdig) {
            return StepType.success;
        }
        switch (tilstandResultat) {
            case "IKKE_STARTET":
                return StepType.default;
            case "OK":
                return StepType.success;
            case "VALIDERING_FEILET":
                return StepType.warning;
            default:
                return StepType.default;
        }
    };

    const steptypeForOpplysninger = () => {
        if (sak?.tilstand.vedtaksbrevBruker === "IKKE_STARTET") {
            return StepType.success;
        }
        return calculateStepType(sak?.tilstand.opplysninger);
    };
    return (
        <>
            <PersonHeader maskertPersonId={sak.maskertPersonIdent} />
            <Box borderWidth="0 0 1 0" borderColor="neutral-subtle" asChild>
                <HStack justify="space-between" align="center">
                    <ProcessMenu>
                        <ProcessMenu.Item
                            label={"Opplysninger"}
                            stepType={steptypeForOpplysninger()}
                            to={"/sak/$saksnummer/opplysninger"}
                        />
                        <ProcessMenu.Item
                            label={"Brev til bruker"}
                            stepType={calculateStepType(sak?.tilstand.vedtaksbrevBruker)}
                            to={"/sak/$saksnummer/vedtaksbrevbruker"}
                            disabled={calculateStepType(sak?.tilstand.opplysninger) !== StepType.success}
                        />
                        <ProcessMenu.Item
                            label={"Oppsummering"}
                            stepType={calculateStepType(sak?.tilstand.oppsummering)}
                            to={"/sak/$saksnummer/oppsummering"}
                            disabled={
                                sak.status !== "TIL_ATTESTERING" &&
                                calculateStepType(sak?.tilstand.vedtaksbrevBruker) !== StepType.success
                            }
                        />
                    </ProcessMenu>
                    <BehandlingsMeny sak={sak} />
                </HStack>
            </Box>

            <RfcErrorBoundary>
                <SakAlert sak={sak} />
                <DeltVisning>
                    <DeltVisning.Kolonne>
                        <Outlet />
                    </DeltVisning.Kolonne>
                    <DeltVisning.Kolonne justerbar>
                        <SakOppsummering sak={sak} />
                        <Tabs defaultValue="dokumenter" style={{ height: "100%" }}>
                            <Tabs.List>
                                <Tabs.Tab value="dokumenter" label="Dokumenter" icon={<FilePdfIcon aria-hidden />} />
                                <Tabs.Tab value="historikk" label="Sakshistorikk" icon={<TasklistIcon aria-hidden />} />
                            </Tabs.List>
                            <Tabs.Panel value="dokumenter" style={{ height: "100%" }}>
                                <Box width="100%" height="100%" paddingBlock="space-16 space-0">
                                    <DokumentViewer saksnummer={saksnummer} />
                                </Box>
                            </Tabs.Panel>
                            <Tabs.Panel value="historikk">
                                <Box width="100%" height="6rem" paddingBlock="space-16 space-0">
                                    <SakshistorikkSakTabell maskertPersonIdent={sak.maskertPersonIdent} />
                                </Box>
                            </Tabs.Panel>
                        </Tabs>
                    </DeltVisning.Kolonne>
                </DeltVisning>
            </RfcErrorBoundary>
        </>
    );
}
