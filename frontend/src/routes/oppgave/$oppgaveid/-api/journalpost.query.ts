import {hentJournalpostMetaDataOptions} from "@generated/@tanstack/react-query.gen";

export const hentJournalpostMetadataQuery = (journalpostId?: string) => ({
    ...hentJournalpostMetaDataOptions({
        path: {
            journalpostId: journalpostId || ''
        }
    }),
    enabled: !!journalpostId,
});