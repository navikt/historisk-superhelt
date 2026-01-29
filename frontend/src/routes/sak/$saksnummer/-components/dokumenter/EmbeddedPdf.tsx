import {Skeleton} from "@navikt/ds-react";

export function EmbeddedPdf(props: { journalpostId: string| undefined , dokumentInfoId: string | undefined }) {

    if (!props.journalpostId || !props.dokumentInfoId) {
        return <Skeleton variant="rectangle" width="100%" height={300} />
    }

    return <embed
        src={`/api/journalpost/${encodeURIComponent(props.journalpostId)}/${encodeURIComponent(props.dokumentInfoId)}`}
        width="100%"
        height="1200px"
        type="application/pdf"
        title="Embedded PDF Viewer"
    />;
}