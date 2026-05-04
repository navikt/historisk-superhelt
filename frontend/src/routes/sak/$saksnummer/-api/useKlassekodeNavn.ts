import type { KlassekodeType, StonadType } from "~/common/sak/sak.types";
import { useStonadsType } from "~/common/sak/useStonadsType";

export function useKlassekodeTypeNavn(type: StonadType) {
    const stonadsType = useStonadsType();
    return (kode: KlassekodeType) => {
        const klasseKoder = stonadsType(type)?.klasseKoder;
        return klasseKoder?.find((k) => k.klasseKode === kode)?.navn ?? kode;
    };
}
