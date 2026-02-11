import {BodyLong, Process} from '@navikt/ds-react';
import {Sak} from '@generated';
import {useSuspenseQuery} from "@tanstack/react-query";
import {hentEndringsloggForSakOptions} from "@generated/@tanstack/react-query.gen";
import {isoTilLokal} from "~/common/dato.utils";
import {
    ArrowCirclepathReverseIcon,
    ArrowRightIcon,
    CheckmarkCircleIcon,
    ChevronRightIcon,
    HourglassBottomFilledIcon,
    PersonPencilIcon,
    SackKronerIcon,
    SparkLargeIcon,
    ThumbDownIcon,
    ThumbUpIcon
} from "@navikt/aksel-icons";
import {EndringsloggType} from "~/routes/sak/$saksnummer/-types/endringslogg.types";

interface SakEndringerProps {
    sak: Sak;
}

export default function SakEndringer({sak}: SakEndringerProps) {
    const {data: endringslogg} = useSuspenseQuery(hentEndringsloggForSakOptions({
        path: {
            saksnummer: sak.saksnummer
        }

    }));

    const getBullet = (type: EndringsloggType) => {
        switch (type) {
            case "DOKUMENT_MOTTATT":
                return <ArrowRightIcon />;
            case "UTBETALING_OK":
                // subtask
                return undefined
            case "UTBETALING_FEILET":
                return <SackKronerIcon />
            case "OPPDATERTE_SAKSDETALJER":
                return <PersonPencilIcon/>;
            case "TIL_ATTESTERING":
                return <HourglassBottomFilledIcon/>;
            case "ATTESTERT_SAK":
                return <ThumbUpIcon/>
            case "FERDIGSTILT_SAK":
                return <CheckmarkCircleIcon/>
            case "ATTESTERING_UNDERKJENT":
                return <ThumbDownIcon/>
            case "GJENAPNET_SAK":
               return <ArrowCirclepathReverseIcon/>
            case "SENDT_BREV":
                // subtask
                return undefined
            case "OPPRETTET_SAK":
                return <SparkLargeIcon />
            default:
                return <ChevronRightIcon/>;

        }


    }
    return (
        <Process>
            {endringslogg?.map((loggLinje) => (
                    <Process.Event
                        key={loggLinje.type + loggLinje.endretTidspunkt}
                        status="completed"
                        title={loggLinje.endring}
                        timestamp={`${isoTilLokal(loggLinje.endretTidspunkt)} av ${loggLinje.endretAv}`}
                        bullet={getBullet(loggLinje.type)}
                    >
                        <BodyLong>  {loggLinje.beskrivelse}</BodyLong>
                    </Process.Event>
                )
            )}

        </Process>
    );
}
