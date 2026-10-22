export interface Standardtekst {
    id: string;
    title: string;
    html: string;
    tags: string[];
    target: "general" | "stondad";
}

export interface StandardtekstResponse {
    data: Standardtekst[] | undefined;
    isPending: boolean;
    isError: boolean;
    error?: Error;
}
