import { hentJournalpostMetaDataOptions } from "@generated/@tanstack/react-query.gen";
import { Box, InlineMessage } from "@navikt/ds-react";
import { useSuspenseQuery } from "@tanstack/react-query";
import { MultiPdfViewer } from "~/common/pdf/MultiPdfViewer";
import styles from "./PdfViewer.module.css";

interface Props {
    journalpostId?: string | null;
}

export function PdfViewer({ journalpostId }: Props) {
    if (!journalpostId) {
        return <InlineMessage status="warning">Det er ikke noe dokument å vise frem</InlineMessage>;
    }
    return <PdfViewerMedData journalpostId={journalpostId} />;
}

function PdfViewerMedData({ journalpostId }: { journalpostId: string }) {
    const { data: journalpost } = useSuspenseQuery(
        hentJournalpostMetaDataOptions({
            path: { journalpostId },
        }),
    );

    return (
        <Box className={styles.pdfViewer}>
            <MultiPdfViewer journalPoster={[journalpost]} />
        </Box>
    );
}
