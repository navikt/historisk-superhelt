import { getWebInstrumentations, initializeFaro } from "@grafana/faro-web-sdk";

export function initialiserFaro() {
    try {
        initializeFaro({
            url: import.meta.env.PROD
                ? "https://telemetry.nav.no/collect"
                : "https://telemetry.ekstern.dev.nav.no/collect",
            app: {
                name: "superhelt",
                namespace: "historisk",
                version: import.meta.env.COMMIT_SHA,
            },
            instrumentations: [...getWebInstrumentations()],
            beforeSend: (item) => {
                const payload = JSON.stringify(item);
                /* Sjekk for 11-sifrede tall som ikke er en del av et lengre tall, 
                kan være personnummer, og fjern hele item hvis det finnes. */
                if (/(?<!\d)\d{11}(?!\d)/.test(payload)) {
                    return null;
                }
                return item;
            },
            pageTracking: {
                generatePageId: (location) =>
                    location.pathname
                        .replace(/\/sak\/[^/]+/, "/sak/{saksnummer}")
                        .replace(/\/person\/[^/]+/, "/person/{personid}")
                        .replace(/\/oppgave\/[^/]+/, "/oppgave/{oppgaveid}"),
            },
        });
    } catch (error) {
        console.error("Feil ved initialisering av Faro:", error);
    }
}
