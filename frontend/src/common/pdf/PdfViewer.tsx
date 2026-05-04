import { hentJournalpostMetaDataOptions } from "@generated/@tanstack/react-query.gen";
import { Box, InlineMessage } from "@navikt/ds-react";
import { useSuspenseQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import styles from "./PdfViewer.module.css";
import { DokumentTabell } from "./DokumentTabell";

interface Props {
    journalpostId?: string | null;
}

export function PdfViewer({ journalpostId }: Props) {
    if (!journalpostId) {
        return <InlineMessage status="warning">Det er ikke noe dokument å vise frem</InlineMessage>;
    }
    return <PdfViewer2 journalpostId={journalpostId} />;
}

function PdfViewer2({ journalpostId }: { journalpostId: string }) {
    const { data: journalpost } = useSuspenseQuery(
        hentJournalpostMetaDataOptions({
            path: {
                journalpostId: journalpostId,
            },
        }),
    );
    const [dokId, setDokId] = useState<string | undefined>(journalpost.dokumenter?.at(0)?.dokumentInfoId);

    useEffect(() => {
        setDokId(journalpost.dokumenter?.at(0)?.dokumentInfoId);
    }, [journalpost.dokumenter]);

    if (!dokId) {
        return <InlineMessage status="warning">Det er ikke noe dokument å vise frem</InlineMessage>;
    }

    return (
        <Box className={styles.pdfViewer}>
            <DokumentTabell
                dokumenter={[journalpost]}
                selected={`${journalpostId}@${dokId}`}
                onSelect={(value) => setDokId(value.split("@")[1])}
            />
            <embed
                src={`/api/journalpost/${encodeURIComponent(journalpostId)}/${encodeURIComponent(dokId)}`}
                width="100%"
                height="1200px"
                type="application/pdf"
                title="Embedded PDF Viewer"
            />
        </Box>
    );
}
