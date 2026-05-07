import { describe, expect, it } from "vitest";
import type { InfotrygdHistorikk, Sak } from "@generated";
import { infotrygdTilHistorikkRad, sakTilHistorikkRad } from "./sakshistorikk.utils";

function createSak(overrides: Partial<Sak> = {}): Sak {
    return {
        saksnummer: "SH-000001",
        status: "UNDER_BEHANDLING",
        vedtaksResultat: "INNVILGET",
        type: "PARYKK",
        utbetalingsType: "BRUKER",
        rettigheter: [],
        klasseKode: "PARYKK",
        beskrivelse: "Beskrivelse",
        soknadsDato: "2026-01-15",
        belop: 5000,
        ...overrides,
    } as Sak;
}

describe("sakTilHistorikkRad", () => {
    it("maps sak to historikkrad", () => {
        const sak = createSak();
        const rad = sakTilHistorikkRad(sak, "Stønad for parykk");

        expect(rad.kilde).toBe("SAK");
        expect(rad.id).toBe("SH-000001");
        expect(rad.kategori).toBe("Stønad for parykk");
        expect(rad.beskrivelse).toBe("Beskrivelse");
        expect(rad.dato).toBe("2026-01-15");
        expect(rad.belop).toBe(5000);
        expect(rad.sak).toBe(sak);
        expect(rad.strekedGjennom).toBe(false);
    });

    it("sets strekedGjennom for FEILREGISTRERT", () => {
        const sak = createSak({ status: "FEILREGISTRERT" });
        const rad = sakTilHistorikkRad(sak, "Test");

        expect(rad.strekedGjennom).toBe(true);
    });

    it("handles null beskrivelse", () => {
        // biome-ignore lint/suspicious/noExplicitAny: testing null value
        const sak = createSak({ beskrivelse: null as any });
        const rad = sakTilHistorikkRad(sak, "Test");

        expect(rad.beskrivelse).toBeUndefined();
    });

    it("handles null soknadsDato", () => {
        // biome-ignore lint/suspicious/noExplicitAny: testing null value
        const sak = createSak({ soknadsDato: null as any });
        const rad = sakTilHistorikkRad(sak, "Test");

        expect(rad.dato).toBeUndefined();
    });
});

describe("infotrygdTilHistorikkRad", () => {
    it("maps infotrygd historikk to rad", () => {
        const h: InfotrygdHistorikk = {
            kontonavn: "Grunnstønad",
            tekst: "Vedtak",
            dato: "1980-05-01",
            fom: "1980-01-01",
            belop: "3000",
        } as InfotrygdHistorikk;

        const rad = infotrygdTilHistorikkRad(h, 0);

        expect(rad.kilde).toBe("INFOTRYGD");
        expect(rad.id).toBe("infotrygd-0");
        expect(rad.kategori).toBe("Grunnstønad");
        expect(rad.beskrivelse).toBe("Vedtak");
        expect(rad.dato).toBe("1980-05-01");
        expect(rad.belop).toBe(3000);
        expect(rad.strekedGjennom).toBe(false);
    });

    it("uses fom when dato is missing", () => {
        const h: InfotrygdHistorikk = {
            kontonavn: "Test",
            dato: null,
            fom: "1980-04-01",
            belop: null,
        } as unknown as InfotrygdHistorikk;

        const rad = infotrygdTilHistorikkRad(h, 5);

        expect(rad.dato).toBe("1980-04-01");
        expect(rad.id).toBe("infotrygd-5");
    });

    it("handles null belop", () => {
        const h: InfotrygdHistorikk = {
            kontonavn: "Test",
            dato: "1980-05-01",
            belop: null,
        } as unknown as InfotrygdHistorikk;

        const rad = infotrygdTilHistorikkRad(h, 0);

        expect(rad.belop).toBeUndefined();
    });
});
