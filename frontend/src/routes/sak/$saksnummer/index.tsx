import { createFileRoute, redirect } from "@tanstack/react-router";
import { getSakOptions } from "./-api/sak.query";

export const Route = createFileRoute("/sak/$saksnummer/")({
    loader: async ({ params: { saksnummer }, context }) => {
        const sak = await context.queryClient.ensureQueryData(getSakOptions(saksnummer));

        if (sak.tilstand.opplysninger !== "OK") {
            throw redirect({ to: "/sak/$saksnummer/opplysninger", params: { saksnummer }, replace: true });
        }

        if (sak.tilstand.vedtaksbrevBruker !== "OK") {
            throw redirect({ to: "/sak/$saksnummer/vedtaksbrevbruker", params: { saksnummer }, replace: true });
        }

        if (sak.tilstand.oppsummering !== "OK") {
            throw redirect({ to: "/sak/$saksnummer/oppsummering", params: { saksnummer }, replace: true });
        }

        throw redirect({ to: "/sak/$saksnummer/oppsummering", params: { saksnummer }, replace: true });
    },
});

