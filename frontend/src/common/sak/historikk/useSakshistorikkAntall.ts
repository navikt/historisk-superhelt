import type { Sak } from "@generated";
import { hentSakHistorikkForPersonOptions } from "@generated/@tanstack/react-query.gen";
import { useSuspenseQuery } from "@tanstack/react-query";
import { isSakFerdig } from "~/common/sak/sak.utils";

type Variant = "ferdig" | "aapen";

//TODO denne må vel kunne forenkles noe? Kanskje flytte filterlogikk inn i en hook og gi tilbake antall og sånt fra det?
/**
 * Felles hook for å beregne antall sakshistorikk-innslag og fane-label.
 *
 * - "ferdig": teller saker med status FERDIG (ekskluderer FEILREGISTRERT) + infotrygd-innslag
 * - "aapen": teller saker som ikke er ferdig/feilregistrert (åpne saker)
 */
export function useSakshistorikkAntall(maskertPersonIdent: string, variant: Variant) {
    const { data, isSuccess } = useSuspenseQuery({
        ...hentSakHistorikkForPersonOptions({ path: { maskertPersonIdent: maskertPersonIdent } }),
    });

    const filtrerSaker: (sak: Sak) => boolean =
        variant === "ferdig" ? (sak) => sak.status === "FERDIG" : (sak) => !isSakFerdig(sak);

    const antallSakshistorikk = isSuccess
        ? (data.saker?.filter(filtrerSaker).length ?? 0) + (variant === "ferdig" ? (data.infotrygd?.length ?? 0) : 0)
        : undefined;

    const sakshistorikkLabel =
        antallSakshistorikk !== undefined ? `Sakshistorikk (${antallSakshistorikk})` : "Sakshistorikk";

    return { antallSakshistorikk, sakshistorikkLabel };
}
