import type {Sak} from "@generated";
import {Box, Detail, Heading, HGrid, HStack, Tag, VStack} from "@navikt/ds-react";
import {isoTilLokal} from "~/common/dato.utils";
import {isSakFerdig, utbetalingText, vedtakAvslatt} from "~/common/sak/sak.utils";
import {useSakVedtakNavn} from "~/common/sak/useSakVedtakNavn";
import {useStonadsTypeNavn} from "~/common/sak/useStonadsTypeNavn";
import {formatertValuta} from "~/common/string.utils";
import styles from "~/routes/sak/$saksnummer/-components/SakOppsummering.module.css";
import SakStatus from "~/routes/sak/$saksnummer/-components/SakStatus";

interface Props {
    sak: Sak;
}

export default function SakOppsummering({ sak }: Props) {
    const getStonadsTypeNavn = useStonadsTypeNavn();
    const getSakVedtakNavn = useSakVedtakNavn();

    const utbetalingsType = vedtakAvslatt(sak) ? null : sak.utbetalingsType;

    const textUtbetaling = () => {
        if (vedtakAvslatt(sak)) {
            return "-";
        }
        return utbetalingText(sak.utbetalingsType);
    };

    return (
        <VStack gap="space-8" className={styles.container}>
            <Box borderColor="neutral-subtle" borderWidth="0 0 1 0" asChild>
                <HStack gap="space-8" align="center" paddingBlock="space-4">
                    <Heading size="small">{sak.beskrivelse}</Heading>
                    <Tag variant="outline" data-color={"info"} size="small">
                        {getStonadsTypeNavn(sak.type)}
                    </Tag>
                    <SakStatus sak={sak} />
                    {!isSakFerdig(sak) && sak.gjenapnet && (
                        <Tag variant="outline" size="small" data-color="accent">
                            Gjenåpnet
                        </Tag>
                    )}
                </HStack>
            </Box>

            <HGrid className={styles.grid}>
                <Detail textColor="subtle">Søknadsdato:</Detail>
                <Detail>{isoTilLokal(sak.soknadsDato)}</Detail>

                <Detail textColor="subtle">Resultat:</Detail>
                <Detail>{sak.vedtaksResultat ? getSakVedtakNavn(sak.vedtaksResultat) : "-"}</Detail>

                <Detail textColor="subtle">Utbetaling:</Detail>
                <Detail>{textUtbetaling()}</Detail>

                {utbetalingsType === "BRUKER" && (
                    <>
                        <Detail textColor="subtle">Beløp:</Detail>
                        <Detail>{formatertValuta(sak.belop)}</Detail>
                    </>
                )}
            </HGrid>
        </VStack>
    );
}
