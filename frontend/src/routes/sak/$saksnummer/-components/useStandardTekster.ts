import type { Standardtekst, StandardtekstSearchFilter } from "@superhelt/breveditor";
import { useCallback } from "react";
import { type StandardtekstForSakstype, standardtekster } from "./standardtekster";

export function searchStandardtekster(
    tekster: StandardtekstForSakstype[],
    query: string,
    caseType: string,
): Standardtekst[] {
    const normalizedQuery = query.trim().toLocaleLowerCase("nb-NO");

    return tekster
        .map((tekst, index) => {
            const title = tekst.title.toLocaleLowerCase("nb-NO");
            const tags = tekst.tags.map((tag) => tag.toLocaleLowerCase("nb-NO"));
            const content = tekst.html.toLocaleLowerCase("nb-NO");
            const relevance = !normalizedQuery
                ? 1
                : title.includes(normalizedQuery)
                  ? 3
                  : tags.some((tag) => tag.includes(normalizedQuery))
                    ? 2
                    : content.includes(normalizedQuery)
                      ? 1
                      : 0;
            return { tekst, index, relevance };
        })
        .filter(({ tekst, relevance }) => (!tekst.caseType || tekst.caseType === caseType) && relevance > 0)
        .sort(
            (a, b) =>
                b.relevance - a.relevance ||
                Number(Boolean(b.tekst.caseType)) - Number(Boolean(a.tekst.caseType)) ||
                a.index - b.index,
        )
        .map(({ tekst }) => tekst);
}

export function useStandardTekster(caseType: string) {
    return useCallback(
        async (filter: StandardtekstSearchFilter): Promise<Standardtekst[]> => {
            return searchStandardtekster(standardtekster, filter.query, caseType).map(({ id, title, html, tags }) => ({
                id,
                title,
                html,
                tags,
            }));
        },
        [caseType],
    );
}
