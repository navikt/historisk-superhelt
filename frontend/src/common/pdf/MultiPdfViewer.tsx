import type { Journalpost } from "@generated";
import { InlineMessage, Select, Skeleton, VStack } from "@navikt/ds-react";
import { useState } from "react";
import { EmbeddedPdf } from "~/common/pdf/EmbeddedPdf";

interface Props {
    journalPoster: Array<Journalpost>;
    laster?: boolean;
}

interface JournalpostDokument {
    journalpostId: string;
    dokumentInfoId: string;
    journalpostTittel?: string | null;
    dokumentTittel?: string | null;
}

const generateDokId = (dok?: JournalpostDokument): string | undefined => {
    if (!dok) return undefined;
    return `${dok.journalpostId}@${dok.dokumentInfoId}`;
};

const getTitle = (d: JournalpostDokument, index: number) => {
    const { dokumentTittel, journalpostTittel } = d;
    if (!dokumentTittel) {
        return `Dokument ${index + 1}`;
    }
    if (dokumentTittel === journalpostTittel) {
        return `${dokumentTittel}`;
    }
    return `${d.journalpostTittel} - ${d.dokumentTittel}`;
};

export function MultiPdfViewer({ journalPoster, laster }: Props) {
    const dokumenter: Array<JournalpostDokument> = journalPoster.flatMap((jp) =>
        (jp.dokumenter || []).map((d) => ({
            journalpostId: jp.journalpostId,
            dokumentInfoId: d.dokumentInfoId,
            journalpostTittel: jp.tittel,
            dokumentTittel: d.tittel,
        })),
    );

    const [selected, setSelected] = useState<string | undefined>(generateDokId(dokumenter.at(0)));
    const [journalpostId, dokId] = selected ? selected.split("@") : [undefined, undefined];

    if (journalPoster.length === 0 || dokumenter.length === 0) {
        return <InlineMessage status="warning">Det er ikke noe dokument å vise frem</InlineMessage>;
    }

    if (laster) {
        return (
            <VStack gap="space-8">
                <Skeleton variant="rounded" height={50} />
                <Skeleton variant="rectangle" height={800} />
            </VStack>
        );
    }

    return (
        <VStack gap="space-8" align="stretch">
            <Select label="Dokumenter i saken" hideLabel value={selected} onChange={(e) => setSelected(e.target.value)}>
                {dokumenter.map((d, index) => (
                    <option key={generateDokId(d)} value={generateDokId(d) ?? ""}>
                        {getTitle(d, index)}
                    </option>
                ))}
            </Select>
            <EmbeddedPdf journalpostId={journalpostId} dokumentInfoId={dokId} />
        </VStack>
    );
}
