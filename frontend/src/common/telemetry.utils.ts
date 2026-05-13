import { getWebInstrumentations, initializeFaro } from "@grafana/faro-web-sdk";
import type { AnyRouter } from "@tanstack/react-router";

export function initialiserFaro(router: AnyRouter) {
    try {
        initializeFaro({
            paused: window.location.hostname === "localhost", // pause på localhost
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
                generatePageId: (location) => {
                    /* grupperer dynamiske url-er basert på routeren,
                    slik at feks. /person/123 og /person/456 blir /person/{personid} */
                    const matched = router.matchRoutes(location.pathname, {});
                    const lastMatch = matched.at(-1);
                    return lastMatch?.fullPath ?? location.pathname;
                },
            },
        });
    } catch (error) {
        console.error("Feil ved initialisering av Faro:", error);
    }
}
