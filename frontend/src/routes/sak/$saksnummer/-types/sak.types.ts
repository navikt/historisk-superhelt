import {Sak as SakDto} from "@generated";

export type SakStatusType = SakDto['status']
export type SakVedtakType = SakDto['vedtaksResultat']
export type StonadType = SakDto['type']
export type UtbetalingsType = SakDto['utbetalingsType']
export type RettighetType = SakDto['rettigheter'][0]