import type { Standardtekst } from "./Standardtekst.types";

export function searchStandardtekster(standardtekster: Standardtekst[], query: string): Standardtekst[] {
    const normalizedQuery = query.trim().toLocaleLowerCase("nb-NO");

    return standardtekster
        .map((standardtekst, index) => {
            const title = standardtekst.title.toLocaleLowerCase("nb-NO");
            const tags = standardtekst.tags.map((tag) => tag.toLocaleLowerCase("nb-NO"));
            const content = standardtekst.html.replace(/<[^>]*>/g, "").toLocaleLowerCase("nb-NO");
            const relevance = !normalizedQuery
                ? 1
                : title.includes(normalizedQuery)
                  ? 3
                  : tags.some((tag) => tag.includes(normalizedQuery))
                    ? 2
                    : content.includes(normalizedQuery)
                      ? 1
                      : 0;

            return { standardtekst, index, relevance };
        })
        .filter(({ relevance }) => relevance > 0)
        .sort(
            (a, b) =>
                Number(Boolean(b.standardtekst.stonadstype)) - Number(Boolean(a.standardtekst.stonadstype)) ||
                b.relevance - a.relevance ||
                a.index - b.index,
        )
        .map(({ standardtekst }) => standardtekst);
}
