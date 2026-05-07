import type {InfotrygdHistorikk, Sak} from "@generated";
import type {HistorikkRad} from "./sakshistorikk.types";

export function sakTilHistorikkRad(sak: Sak, kategoriNavn: string): HistorikkRad {
    return {
        kilde: "SAK",
        id: sak.saksnummer,
        kategori: kategoriNavn,
        beskrivelse: sak.beskrivelse ?? undefined,
        dato: sak.soknadsDato ?? undefined,
        belop: sak.belop ?? undefined,
        sak,
        strekedGjennom: sak.status === "FEILREGISTRERT",
    };
}

export function infotrygdTilHistorikkRad(h: InfotrygdHistorikk, index: number): HistorikkRad {
    return {
        kilde: "INFOTRYGD",
        id: `infotrygd-${index}`,
        kategori: h.kontonavn,
        beskrivelse: h.tekst ?? undefined,
        dato: h.dato ?? h.fom?? undefined,
        belop: h.belop != null ? Number(h.belop) : undefined,
        strekedGjennom: false,
    };
}
