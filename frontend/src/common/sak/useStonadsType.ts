import { useSuspenseQuery } from "@tanstack/react-query";
import type { StonadType } from "~/common/sak/sak.types";
import { getKodeverkStonadsTypeOptions } from "./sak.query";

export function useStonadsType() {
    const { data: stonadsTyper } = useSuspenseQuery(getKodeverkStonadsTypeOptions());
    return (type?: StonadType) => {
        if (!type) {
            return undefined;
        }
        return stonadsTyper.find((t) => t.type === type);
    };
}
