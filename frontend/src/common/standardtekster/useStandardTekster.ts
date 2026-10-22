import type { Standardtekst, StandardtekstResponse } from "@superhelt/breveditor";
import { useMemo } from "react";
import type { StonadType } from "~/common/sak/sak.types";
import { useStonadsType } from "~/common/sak/useStonadsType";
import { standardtekster as standardtekstKatalog } from "./standardtekster";

export function useStandardTekster(caseType: StonadType): StandardtekstResponse {
    const getStonadsType = useStonadsType();
    const stonadstype = getStonadsType(caseType)?.navn;

    return useMemo(() => {
        const data: Standardtekst[] = standardtekstKatalog
            .filter((tekst) => !tekst.saksType || tekst.saksType === caseType)
            .map(({ saksType, ...tekst }) => (saksType && stonadstype ? { ...tekst, stonadstype } : tekst));

        return {
            data,
            isPending: false,
            isError: false,
        };
    }, [caseType, stonadstype]);
}
