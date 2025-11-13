import {Sak as SakDto} from "@api";

export type SakStatusType = SakDto['status']
export type SakVedtakType = SakDto['vedtak']
export type StonadType = SakDto['type']
export type UtbetalingsType = SakDto['utbetalingsType']
export type RettighetType = SakDto['rettigheter'][0]