import {Button} from "@navikt/ds-react";
import {Sak} from "@api";
import {useMutation, useQueryClient} from "@tanstack/react-query";
import {ferdigstillSakMutation} from "@api/@tanstack/react-query.gen";
import {sakQueryKey} from "~/routes/sak/$saksnummer/-api/sak.query";

interface Props {
    sak: Sak
}

export default function SakActionButton({sak}: Props) {
    const queryClient = useQueryClient();
    const saksnummer = sak.saksnummer

    const ferdigStillSak = useMutation({
        ...ferdigstillSakMutation()
        , onSettled: () => {
            queryClient.invalidateQueries({queryKey: sakQueryKey(saksnummer)})
        }
    })

    async function fatteVedtak() {

        //TODO Validering
        ferdigStillSak.mutate({
                path: {
                    saksnummer: saksnummer
                }
            }
        )
    }

    return <Button
        variant="primary"
        disabled={!sak.rettigheter.includes("FERDIGSTILLE")}
        onClick={fatteVedtak}
        loading={ferdigStillSak?.status === 'pending'}
    >
        Fatte vedtak
    </Button>
}