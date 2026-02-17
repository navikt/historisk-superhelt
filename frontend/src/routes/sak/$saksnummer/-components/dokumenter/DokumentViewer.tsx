import {useSuspenseQuery} from "@tanstack/react-query";
import {finnJournalposterForSakOptions} from "@generated/@tanstack/react-query.gen";
import {HStack} from "@navikt/ds-react";
import {MultiPdfViewer} from "~/routes/sak/$saksnummer/-components/dokumenter/MultiPdfViewer";

interface DokumentViewerProps {
    saksnummer: string;
}

export default function DokumentViewer({saksnummer}: DokumentViewerProps) {
    const {data: journalposter} = useSuspenseQuery(({
        ...finnJournalposterForSakOptions({path: {saksnummer: saksnummer}}),
    }))

    return (
        <HStack gap="space-32">
            <MultiPdfViewer journalPoster={journalposter}/>
        </HStack>
    );


}