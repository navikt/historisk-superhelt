import type {InfotrygdHistorikk, Sak} from "@generated";
import type {HistorikkRad} from "./sakshistorikk.types";

export function sakTilHistorikkRad(sak: Sak, kategoriNavn: string): HistorikkRad {
    return {
        kilde: "SAK",
        id: sak.saksnummer,
        kategori: kategoriNavn,
        beskrivelse: sak.beskrivelse,
        dato: sak.soknadsDato,
        belop: sak.belop,
        sak,
        strekedGjennom: sak.status === "FEILREGISTRERT",
    };
}

export function infotrygdTilHistorikkRad(h: InfotrygdHistorikk, index: number): HistorikkRad {
    return {
        kilde: "INFOTRYGD",
        id: `infotrygd-${index}`,
        kategori: h.kontonavn,
        beskrivelse: h.tekst,
        dato: h.dato ?? h.fom,
        belop: h.belop != null ? Number(h.belop) : undefined,
        strekedGjennom: false,
    };
}
