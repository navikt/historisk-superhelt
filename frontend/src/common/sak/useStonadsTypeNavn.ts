import type { StonadType } from "~/common/sak/sak.types";
import { useStonadsType } from "~/common/sak/useStonadsType";

export function useStonadsTypeNavn() {
    const stonadsType = useStonadsType();
    return (type?: StonadType) => {
        if (!type) {
            return "";
        }
        return stonadsType(type)?.navn ?? type;
    };
}
