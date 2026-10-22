import type { Standardtekst, StandardtekstResponse } from "@superhelt/breveditor";
import { useMemo } from "react";
import { standardtekster as standardtekstKatalog } from "./standardtekster";

export function useStandardTekster(caseType: string): StandardtekstResponse {
    return useMemo(() => {
        const data: Standardtekst[] = standardtekstKatalog
            .filter((tekst) => !tekst.caseType || tekst.caseType === caseType)
            .map(({ caseType: tekstCaseType, ...tekst }) => ({
                ...tekst,
                target: tekstCaseType ? "stondad" : "general",
            }));

        return {
            data,
            isPending: false,
            isError: false,
        };
    }, [caseType]);
}
