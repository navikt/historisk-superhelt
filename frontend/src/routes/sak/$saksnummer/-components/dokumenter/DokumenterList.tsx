import {useSuspenseQuery} from "@tanstack/react-query";
import {finnJournalposterForSakOptions} from "@generated/@tanstack/react-query.gen";

interface DokumenterListProps {
    saksnummer: string;
}

export default function DokumenterList({saksnummer}: DokumenterListProps) {
    const {data: journalposter, isPending, error} = useSuspenseQuery(({
        ...finnJournalposterForSakOptions({path: {saksnummer: saksnummer}}),
    }))

    return <ul>
        {journalposter.map((journalpost) => (
                <li key={journalpost.journalpostId}>{journalpost.tittel}</li>
            )
        )}
    </ul>

}