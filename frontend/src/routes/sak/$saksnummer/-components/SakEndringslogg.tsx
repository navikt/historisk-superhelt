import {BodyLong, Process} from '@navikt/ds-react';
import {Sak} from '@generated';
import {useSuspenseQuery} from "@tanstack/react-query";
import {hentEndringsloggForSakOptions} from "@generated/@tanstack/react-query.gen";
import {isoTilLokal} from "~/components/dato.utils";
import {ChevronRightIcon} from "@navikt/aksel-icons";

interface SakEndringerProps {
    sak: Sak;
}

export default function SakEndringer({sak}: SakEndringerProps) {
    const {data: changelog} = useSuspenseQuery(hentEndringsloggForSakOptions({
        path: {
            saksnummer: sak.saksnummer
        }

    }));

    return (
        <Process>
            {changelog?.map((entry, index) => (
                    <Process.Event
                        key={entry.endretTidspunkt}
                        status="completed"
                        title={entry.endring}
                        timestamp={`${isoTilLokal(entry.endretTidspunkt)} av ${entry.endretAv}`}
                        bullet={<ChevronRightIcon/>}
                    >
                        <BodyLong>  {entry.beskrivelse}</BodyLong>
                    </Process.Event>
                )
            )}


        </Process>
    );
}
