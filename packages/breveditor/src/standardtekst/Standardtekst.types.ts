export interface Standardtekst {
    id: string;
    title: string;
    html: string;
    tags: string[];
    stonadstype?: string;
}

export interface StandardtekstResponse {
    data: Standardtekst[] | undefined;
    isPending: boolean;
    isError: boolean;
    error?: Error;
}
