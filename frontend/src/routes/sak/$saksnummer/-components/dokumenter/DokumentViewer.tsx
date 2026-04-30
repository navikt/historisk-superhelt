import { HStack } from "@navikt/ds-react";
import { useSuspenseQuery } from "@tanstack/react-query";
import { apiFinnJournalposterOptions } from "~/routes/sak/$saksnummer/-api/journalpost.query";
import { MultiPdfViewer } from "~/common/pdf/MultiPdfViewer";

interface DokumentViewerProps {
    saksnummer: string;
}

export default function DokumentViewer({ saksnummer }: DokumentViewerProps) {
    const { data: journalposter } = useSuspenseQuery(apiFinnJournalposterOptions(saksnummer, false));

    return (
        <HStack gap="space-32">
            <MultiPdfViewer journalPoster={journalposter} />
        </HStack>
    );
}
