import {useSuspenseQuery} from "@tanstack/react-query";
import {finnJournalposterForSakOptions} from "@generated/@tanstack/react-query.gen";
import {HStack} from "@navikt/ds-react";
import {PdfViewer} from "~/common/pdf/PdfViewer";
import {useState} from "react";

interface DokumnetViewerProps {
    saksnummer: string;
}

export default function DokumentViewer({saksnummer}: DokumnetViewerProps) {
    const {data: journalposter, isPending, error} = useSuspenseQuery(({
        ...finnJournalposterForSakOptions({path: {saksnummer: saksnummer}}),
    }))

    const [selectedJournalpost, setSelectedJournalpost] = useState<string | undefined>(journalposter.at(0)?.journalpostId)

    return <HStack gap="8">
        <ul>
            {journalposter.map((journalpost) => (
                    <li key={journalpost.journalpostId} onClick={()=>setSelectedJournalpost(journalpost.journalpostId)}>{journalpost.tittel}</li>
                )
            )}
        </ul>

        <PdfViewer journalpostId={selectedJournalpost} />
    </HStack>


}