package no.nav.historisk.superhelt.sak

import no.nav.common.types.NorskeKroner

object SakExtensions {
    fun Sak.getBelop(): NorskeKroner? {
        return when (this.utbetalingsType) {
            UtbetalingsType.BRUKER -> this.utbetaling?.belop
            UtbetalingsType.FORHANDSTILSAGN -> this.forhandstilsagn?.belop
            UtbetalingsType.INGEN -> null
        }

    }
}