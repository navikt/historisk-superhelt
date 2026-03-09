import { describe, it, expect } from "vitest";
import {
  beregnKostnad,
  beregnReisetid,
  simulerAvstand,
  KR_PER_KM,
  MIN_PER_KM,
} from "./calculateCost";

describe("beregnKostnad", () => {
  it("beregner korrekt kostnad for gitt avstand", () => {
    expect(beregnKostnad(10)).toBe(10 * KR_PER_KM);
    expect(beregnKostnad(0)).toBe(0);
    expect(beregnKostnad(100)).toBe(100 * KR_PER_KM);
  });

  it("runder til to desimaler", () => {
    const result = beregnKostnad(7);
    expect(result).toBe(Math.round(7 * KR_PER_KM * 100) / 100);
  });
});

describe("beregnReisetid", () => {
  it("beregner korrekt reisetid for gitt avstand", () => {
    expect(beregnReisetid(10)).toBe(Math.round(10 * MIN_PER_KM));
    expect(beregnReisetid(0)).toBe(0);
  });
});

describe("simulerAvstand", () => {
  it("returnerer en avstand mellom 5 og 50 km", () => {
    const avstand = simulerAvstand("Alna helsestasjon", "Frogner helsestasjon");
    expect(avstand).toBeGreaterThanOrEqual(5);
    expect(avstand).toBeLessThanOrEqual(50);
  });

  it("returnerer samme avstand for samme par", () => {
    const a = simulerAvstand("Alna helsestasjon", "Frogner helsestasjon");
    const b = simulerAvstand("Alna helsestasjon", "Frogner helsestasjon");
    expect(a).toBe(b);
  });

  it("er ikke sensitiv for rekkefølge på mellomrom og store/små bokstaver", () => {
    const a = simulerAvstand("  Alna Helsestasjon  ", "frogner helsestasjon");
    const b = simulerAvstand("alna helsestasjon", "FROGNER HELSESTASJON");
    expect(a).toBe(b);
  });
});
