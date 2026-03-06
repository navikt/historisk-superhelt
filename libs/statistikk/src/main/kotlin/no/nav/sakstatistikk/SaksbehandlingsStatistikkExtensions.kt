package no.nav.sakstatistikk

import SaksbehandlingsStatistikk

/**
 * Extension-funksjoner som lar konsumenter bruke egne enums i stedet for rå String-verdier.
 * Enum-verdiens `.name` brukes som statistikk-kode, slik at lib-en forblir String-basert.
 *
 * Eksempel:
 * ```kotlin
 * enum class BehandlingResultat { INNVILGET, AVSLATT }
 *
 * val statistikk = SaksbehandlingsStatistikk(...)
 *     .medBehandlingResultat(BehandlingResultat.INNVILGET)
 *     .medBehandlingStatus(MinBehandlingStatus.AVSLUTTET)
 * ```
 */

fun SaksbehandlingsStatistikk.medBehandlingResultat(resultat: Enum<*>?) =
    copy(behandlingResultat = resultat?.name)

fun SaksbehandlingsStatistikk.medResultatBegrunnelse(begrunnelse: Enum<*>?) =
    copy(resultatBegrunnelse = begrunnelse?.name)

fun SaksbehandlingsStatistikk.medBehandlingStatus(status: Enum<*>) =
    copy(behandlingStatus = status.name)

fun SaksbehandlingsStatistikk.medBehandlingType(type: Enum<*>) =
    copy(behandlingType = type.name)

fun SaksbehandlingsStatistikk.medBehandlingAarsak(aarsak: Enum<*>?) =
    copy(behandlingAarsak = aarsak?.name)
