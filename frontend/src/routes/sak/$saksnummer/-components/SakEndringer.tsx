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
    const {data: endringslogg} = useSuspenseQuery(hentEndringsloggForSakOptions({
        path: {
            saksnummer: sak.saksnummer
        }

    }));

    return (
        <Process>
            {endringslogg?.map((loggLinje) => (
                    <Process.Event
                        key={loggLinje.type + loggLinje.endretTidspunkt}
                        status="completed"
                        title={loggLinje.endring}
                        timestamp={`${isoTilLokal(loggLinje.endretTidspunkt)} av ${loggLinje.endretAv}`}
                        bullet={<ChevronRightIcon/>}
                    >
                        <BodyLong>  {loggLinje.beskrivelse}</BodyLong>
                    </Process.Event>
                )
            )}

        </Process>
    );
}
