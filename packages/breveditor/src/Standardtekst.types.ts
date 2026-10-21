export interface Standardtekst {
    id: string;
    title: string;
    html: string;
    tags: string[];
}

export interface StandardtekstSearchFilter {
    query: string;
}

export type StandardtekstSearch = (filter: StandardtekstSearchFilter) => Promise<Standardtekst[]>;
