import type { Sak } from "@generated";
import { getSakStatusOptions } from "@generated/@tanstack/react-query.gen";
import { HStack, Tag } from "@navikt/ds-react";
import { useSuspenseQuery } from "@tanstack/react-query";
import { SakStatusTag } from "~/common/sak/SakStatusTag";
import { useSakVedtakNavn } from "~/common/sak/useSakVedtakNavn";

interface Props {
    sak: Sak;
}

export default function SakStatus({ sak }: Props) {
    const { data: sakStatus } = useSuspenseQuery(getSakStatusOptions({ path: { saksnummer: sak.saksnummer } }));
    const getSakVedtakNavn = useSakVedtakNavn();
    const hasUtbetalingsFeil = sakStatus.aggregertStatus === "FEILET";

    if (sak.status !== "FERDIG") {
        return <SakStatusTag status={sak.status} />;
    }

    const vedtaksResultatColor =
        sak.vedtaksResultat === "INNVILGET" || sak.vedtaksResultat === "DELVIS_INNVILGET" ? "success" : "neutral";

    return (
        <HStack gap="2" wrap={false}>
            <Tag data-color={vedtaksResultatColor} variant="moderate" size="small">
                {getSakVedtakNavn(sak.vedtaksResultat)}
            </Tag>
            {hasUtbetalingsFeil && (
                <Tag variant="error" size="small">
                    Utbetalingsfeil
                </Tag>
            )}
        </HStack>
    );
}
