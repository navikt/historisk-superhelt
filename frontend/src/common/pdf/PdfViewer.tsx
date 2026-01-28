import {Box, InlineMessage, Select} from '@navikt/ds-react'
import {useEffect, useState} from 'react'
import styles from './PdfViewer.module.css'
import {useSuspenseQuery} from "@tanstack/react-query";
import {hentJournalpostMetaDataOptions} from "@generated/@tanstack/react-query.gen";

interface Props {
    journalpostId?: string
}

export function PdfViewer({journalpostId}: Props) {
    if (!journalpostId) {
        return <InlineMessage status="warning">Det er ikke noe dokument å vise frem</InlineMessage>
    }
    return <PdfViewer2 journalpostId={journalpostId}/>
}

function PdfViewer2({journalpostId}: { journalpostId: string }) {
    const {data: journalpost} = useSuspenseQuery(hentJournalpostMetaDataOptions({
        path: {
            journalpostId: journalpostId
        }
    }))
    const [dokId, setDokId] = useState<string | undefined>(journalpost.dokumenter?.at(0)?.dokumentInfoId)

    useEffect(() => {
        setDokId(journalpost.dokumenter?.at(0)?.dokumentInfoId)
    }, [journalpostId, journalpost.dokumenter])


    if (!dokId) {
        return <InlineMessage status="warning">Det er ikke noe dokument å vise frem</InlineMessage>
    }

    return (
        <Box className={styles.pdfViewer}>
            <Select label="Velg dokument" value={dokId} hideLabel onChange={(e) => setDokId(e.target.value)}>
                {(journalpost?.dokumenter || []).map((d, index) => (
                    <option key={d.dokumentInfoId} value={d.dokumentInfoId}>
                        Dokument {index + 1} av {(journalpost?.dokumenter || []).length} - {d.tittel}
                    </option>
                ))}
            </Select>
            <embed
                src={`/api/journalpost/${encodeURIComponent(journalpostId)}/${encodeURIComponent(dokId)}`}
                width="100%"
                height="1200px"
                type="application/pdf"
                title="Embedded PDF Viewer"
            />
        </Box>
    )
}
