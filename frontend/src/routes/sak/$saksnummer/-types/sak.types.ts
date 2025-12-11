import {Sak, SakTilstand} from "@generated";

export type SakStatusType = Sak['status']
export type SakVedtakType = Sak['vedtaksResultat']
export type StonadType = Sak['type']
export type UtbetalingsType = Sak['utbetalingsType']
export type RettighetType = Sak['rettigheter'][0]
export type TilstandStatusType = SakTilstand["opplysninger"]