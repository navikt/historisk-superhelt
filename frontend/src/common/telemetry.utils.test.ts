import type { AnyRouter } from "@tanstack/react-router";
import { describe, expect, it } from "vitest";
import { genererSideId, inneholderPersonnummer } from "./telemetry.utils";

describe("inneholderPersonnummer", () => {
    it("should match exactly 11 consecutive digits", () => {
        expect(inneholderPersonnummer("12345678901")).toBe(true);
    });

    it("should match 11 digits surrounded by non-digit characters", () => {
        expect(inneholderPersonnummer("fnr:12345678901,")).toBe(true);
        expect(inneholderPersonnummer('"ident":"12345678901"')).toBe(true);
    });

    it("should not match fewer than 11 digits", () => {
        expect(inneholderPersonnummer("1234567890")).toBe(false);
    });

    it("should not match more than 11 digits", () => {
        expect(inneholderPersonnummer("123456789012")).toBe(false);
    });

    it("should not match timestamps (13 digits)", () => {
        expect(inneholderPersonnummer("1778670670894")).toBe(false);
    });

    it("should not match a string without digits", () => {
        expect(inneholderPersonnummer("hello world")).toBe(false);
    });

    it("should match when 11 digits appear inside a larger JSON payload", () => {
        const payload = JSON.stringify({ data: { ident: "12345678901", value: 42 } });
        expect(inneholderPersonnummer(payload)).toBe(true);
    });

    it("should not match when JSON only contains short numbers and timestamps", () => {
        const payload = JSON.stringify({
            timestamp: "2026-05-13T11:12:48.725Z",
            interaction_time: 94733.8,
            duration: 104,
            transferSize: 1225,
        });
        expect(inneholderPersonnummer(payload)).toBe(false);
    });

    it("should not match Faro web-vitals measurement payloads", () => {
        const payload = JSON.stringify({
            type: "measurement",
            payload: {
                type: "web-vitals",
                timestamp: "2026-05-13T11:12:48.725Z",
                values: { inp: 104, delta: 96, interaction_time: 94733.8, presentation_delay: 22.3 },
                context: { id: "v5-1778670670894-2707375970920" },
            },
        });
        expect(inneholderPersonnummer(payload)).toBe(false);
    });
});

function lagMockRouter(ruter: Record<string, string>): AnyRouter {
    return {
        matchRoutes: (pathname: string) => {
            for (const [pattern, fullPath] of Object.entries(ruter)) {
                const regex = new RegExp(`^${pattern.replace(/\$[^/]+/g, "[^/]+")}$`);
                if (regex.test(pathname)) {
                    return [{ fullPath }];
                }
            }
            return [];
        },
    } as unknown as AnyRouter;
}

describe("genererSideId", () => {
    const router = lagMockRouter({
        "/": "/",
        "/sak/$saksnummer": "/sak/$saksnummer",
        "/sak/$saksnummer/opplysninger": "/sak/$saksnummer/opplysninger",
        "/sak/$saksnummer/oppsummering": "/sak/$saksnummer/oppsummering",
        "/sak/$saksnummer/vedtaksbrevbruker": "/sak/$saksnummer/vedtaksbrevbruker",
        "/person/$personid": "/person/$personid",
        "/oppgave/$oppgaveid": "/oppgave/$oppgaveid",
        "/oppgave/$oppgaveid/journalfor": "/oppgave/$oppgaveid/journalfor",
    });

    it("should group dynamic sak routes to the route pattern", () => {
        expect(genererSideId(router, "/sak/SH-000190/opplysninger")).toBe("/sak/$saksnummer/opplysninger");
        expect(genererSideId(router, "/sak/SH-000999/opplysninger")).toBe("/sak/$saksnummer/opplysninger");
    });

    it("should group dynamic person routes to the route pattern", () => {
        expect(genererSideId(router, "/person/abc123")).toBe("/person/$personid");
    });

    it("should group dynamic oppgave routes to the route pattern", () => {
        expect(genererSideId(router, "/oppgave/42/journalfor")).toBe("/oppgave/$oppgaveid/journalfor");
    });

    it("should return the root path as-is", () => {
        expect(genererSideId(router, "/")).toBe("/");
    });

    it("should fall back to raw pathname for unknown routes", () => {
        expect(genererSideId(router, "/ukjent/side")).toBe("/ukjent/side");
    });
});
