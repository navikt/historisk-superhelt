import { hentJournalpostMetaDataOptions } from "@superhelt/api/react-query.gen";

export const hentJournalpostMetadataQuery = (journalpostId?: string | null) => ({
    ...hentJournalpostMetaDataOptions({
        path: {
            journalpostId: journalpostId || "",
        },
    }),
    enabled: !!journalpostId,
});
