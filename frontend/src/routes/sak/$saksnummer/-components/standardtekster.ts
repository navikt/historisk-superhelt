import type { Standardtekst } from "@superhelt/breveditor";

export type StandardtekstForSakstype = Standardtekst & { caseType?: string };

export const standardtekster: StandardtekstForSakstype[] = [
    {
        id: "utbetaling-kontonummer",
        title: "Utbetaling",
        html: "<p>Vi har utbetalt <mark>x xxx kroner</mark> til kontonummeret som er registrert hos Nav.</p>",
        tags: ["Utbetaling"],
    },
    {
        id: "parykk-ordinær-innvilgelse",
        title: "Innvilgelse – stønad for personer over 30 år (ordinær stønad)",
        html: "<p>Du er innvilget stønad til parykk på inntil <mark>6 265</mark> kroner.</p><p>Stønad til parykk følger de satsene som gjelder til enhver tid. Du finner oppdaterte satser på nav.no/satser.</p>",
        tags: ["Parykk", "Innvilgelse", "Stønad for personer over 30 år", "Ordinær stønad"],
        caseType: "PARYKK",
    },
    {
        id: "brystprotese-utbetaling-spesialbrystholder",
        title: "Innvilgelse – utbetaling av spesialbrystholder",
        html: "<p>Vi har mottatt kvittering fra deg på <mark>xxx</mark> kroner som gjelder kjøp av spesialbrystholder. Av dette utbetaler vi <mark>xxx</mark> kroner til ditt kontonummer registrert hos Nav. Beløpet vil være på konto innen 1-4 virkedager.</p>",
        tags: ["Brystprotese", "Spesialbrystholder", "Innvilgelse", "Utbetaling"],
        caseType: "BRYSTPROTESE",
    },
    {
        id: "oyeprotese-innvilgelse",
        title: "Innvilgelse – øyeprotese",
        html: "<p>På grunnlag av foreliggende medisinske opplysninger innvilges stønad til øyeprotese.</p><p>Utgifter til anskaffelse av øyeprotese/skallprotese dekkes fullt ut.</p>",
        tags: ["Øyeprotese", "Innvilgelse"],
        caseType: "OYE_PROTESE",
    },
    {
        id: "ansiktsprotese-innvilgelse",
        title: "Innvilgelse – ansiktsprotese",
        html: "<p>På grunnlag av foreliggende medisinske opplysninger mottatt fra Oslo Universitetssykehus, Rikshospitalet, innvilges stønad til ansiktsdefektprotese.</p><p>Utgifter til ansiktsdefektprotese dekkes fullt ut.</p>",
        tags: ["Ansiktsprotese", "Innvilgelse"],
        caseType: "ANSIKT_PROTESE",
    },
];
