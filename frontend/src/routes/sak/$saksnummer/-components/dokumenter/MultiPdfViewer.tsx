import {Box, InlineMessage, Select} from '@navikt/ds-react'
import {useState} from 'react'
import {Journalpost} from "@generated";
import {EmbeddedPdf} from "~/routes/sak/$saksnummer/-components/dokumenter/EmbeddedPdf";

interface Props {
    journalPoster: Array<Journalpost>
}

interface JournalpostDokument {
    journalpostId: string;
    dokumentInfoId: string;
    journalpostTittel?: string;
    dokumentTittel?: string;
}

const generateDokId = (dok?: JournalpostDokument) => {
    return `${dok?.journalpostId}@${dok?.dokumentInfoId}`;
}

const getTitle = (d: JournalpostDokument, index: number) => {
    const {dokumentTittel, journalpostTittel} = d
    if (!dokumentTittel) {
        return "Dokument " + (index + 1);
    }
    if (dokumentTittel === journalpostTittel) {
        return `${dokumentTittel}`;
    }
    return `${d.journalpostTittel} - ${d.dokumentTittel}`;
}

export function MultiPdfViewer({journalPoster}: Props) {
    const dokumenter: Array<JournalpostDokument> = journalPoster
        .flatMap(jp => (jp.dokumenter || [])
            .map(d => ({
                    journalpostId: jp.journalpostId,
                    dokumentInfoId: d.dokumentInfoId,
                    journalpostTittel: jp.tittel,
                    dokumentTittel: d.tittel
                })
            )
        );
    const firstDokument = dokumenter.at(0);

    const [selected, setSelected] = useState<string | undefined>(generateDokId(firstDokument));
    const [journalpostId, dokId] = selected ? selected.split('@') : [undefined, undefined];


    if (journalPoster.length === 0) {
        return <InlineMessage status="warning">Det er ikke noe dokument å vise frem</InlineMessage>
    }

    return (
        <Box width={"100%"}>
            <Select label="Dokumenter i saken" hideLabel value={selected} onChange={(e) => setSelected(e.target.value)}>
                {dokumenter.map((d, index) => (
                    <option key={generateDokId(d)} value={generateDokId(d)}>
                        {getTitle(d, index)}
                    </option>
                ))}
            </Select>
            <EmbeddedPdf journalpostId={journalpostId} dokumentInfoId={dokId}/>
        </Box>
    )
}
