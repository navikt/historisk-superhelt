import type { Sak } from "@generated";

export type HistorikkRad = {
    kilde: "SAK" | "INFOTRYGD";
    id: string;
    kategori: string;
    beskrivelse?: string;
    dato?: string;
    belop?: number;
    /** Kun for SAK-rader – brukes av SakStatus og navigasjonslenke */
    sak?: Sak;
    strekedGjennom: boolean;
};

export type HistorikkSortKey = "dato" | "kategori" | "belop" | "id";
