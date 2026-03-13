import { createFileRoute, useNavigate } from "@tanstack/react-router";

export const Route = createFileRoute("/sak/$saksnummer/")({
    component: SakIndex,
});

function SakIndex() {
    const { saksnummer } = Route.useParams();
    const navigate = useNavigate();

    navigate({ to: "/sak/$saksnummer/opplysninger", params: { saksnummer }, replace: true });

}
