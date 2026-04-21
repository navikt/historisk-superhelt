import type { Sak } from "@generated";
import {
    findSakerForPersonOptions,
    hentInfotrygdHistorikkForPersonOptions,
} from "@generated/@tanstack/react-query.gen";
import { useQuery } from "@tanstack/react-query";
import { isSakFerdig } from "~/common/sak/sak.utils";

type Variant = "ferdig" | "aapen";

/**
 * Felles hook for å beregne antall sakshistorikk-innslag og fane-label.
 *
 * - "ferdig": teller saker med status FERDIG (ekskluderer FEILREGISTRERT) + infotrygd-innslag
 * - "aapen": teller saker som ikke er ferdig/feilregistrert (åpne saker)
 */
export function useSakshistorikkAntall(maskertPersonIdent: string, variant: Variant) {
    const { data: sakerForPerson, isSuccess: erSakerLastet } = useQuery(
        findSakerForPersonOptions({ query: { maskertPersonId: maskertPersonIdent } }),
    );
    const { data: infotrygdHistorikk, isSuccess: erInfotrygdLastet } = useQuery(
        hentInfotrygdHistorikkForPersonOptions({ path: { maskertPersonIdent } }),
    );

    const filtrerSaker: (sak: Sak) => boolean =
        variant === "ferdig" ? (sak) => sak.status === "FERDIG" : (sak) => !isSakFerdig(sak);

    const antallSakshistorikk =
        erSakerLastet && erInfotrygdLastet
            ? (sakerForPerson?.filter(filtrerSaker).length ?? 0) +
              (variant === "ferdig" ? (infotrygdHistorikk?.length ?? 0) : 0)
            : undefined;

    const sakshistorikkLabel =
        antallSakshistorikk !== undefined ? `Sakshistorikk (${antallSakshistorikk})` : "Sakshistorikk";

    return { antallSakshistorikk, sakshistorikkLabel };
}
