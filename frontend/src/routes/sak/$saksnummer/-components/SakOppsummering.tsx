import type { Sak } from "@generated";
import { Box, Detail, Heading, HGrid, HStack, Tag, VStack } from "@navikt/ds-react";
import { isoTilLokal } from "~/common/dato.utils";
import { isSakFerdig, utbetalingText, vedtakResultatText } from "~/common/sak/sak.utils";
import { useStonadsTypeNavn } from "~/common/sak/useStonadsTypeNavn";
import { enumkodeTilTekst, formatertValuta } from "~/common/string.utils";
import SakStatus from "~/routes/sak/$saksnummer/-components/SakStatus";
import styles from "~/routes/sak/$saksnummer/-components/SakOppsummering.module.css";

interface Props {
    sak: Sak;
}

export default function SakOppsummering({ sak }: Props) {
    const getStonadsTypeNavn = useStonadsTypeNavn();
    return (
        <VStack gap="space-8" className={styles.container}>
            <Box borderColor="neutral-subtle" borderWidth="0 0 1 0" asChild>
                <HStack gap="space-8" align="center" paddingBlock="space-4">
                    <Heading size="small">
                        {sak.beskrivelse ? sak.beskrivelse : "Søknad om " + getStonadsTypeNavn(sak.type)}
                    </Heading>
                    <SakStatus sak={sak} />
                    {!isSakFerdig(sak) && sak.gjenapnet && (
                        <Tag variant="outline" size="small" data-color="accent">
                            Gjenåpnet
                        </Tag>
                    )}
                </HStack>
            </Box>

            <HGrid className={styles.grid} align="center">
                <Detail textColor="subtle">Saksnummer:</Detail>
                <Detail>{sak.saksnummer}</Detail>

                <Detail textColor="subtle">Stønadstype:</Detail>
                <Detail>{getStonadsTypeNavn(sak.type)}</Detail>

                <Detail textColor="subtle">Utbetaling:</Detail>
                <Detail>{sak.belop ? formatertValuta(sak.belop) : "Ikke sendt"}</Detail>

                <Detail textColor="subtle">Søknadsdato:</Detail>
                <Detail>{isoTilLokal(sak.soknadsDato)}</Detail>

                <Detail textColor="subtle">Vedtaksresultat:</Detail>
                <Detail>{sak.vedtaksResultat ? vedtakResultatText(sak.vedtaksResultat) : "Under arbeid"}</Detail>

                <Detail textColor="subtle">Utbetalingstype:</Detail>
                <Detail>{utbetalingText(sak.utbetalingsType)}</Detail>
            </HGrid>
        </VStack>
    );
}
