package no.nav.historisk.superhelt.utbetaling

import no.nav.common.types.Belop.Companion.ZeroBelop
import no.nav.helved.UtbetalingUuid
import no.nav.historisk.superhelt.sak.Sak
import java.util.*

object UtbetalingSakExtensions {

    fun Sak.newUtbetaling(tidligereUtbetaling: Utbetaling? = null) = Utbetaling(
        saksnummer = this.saksnummer,
        behandlingsnummer = this.behandlingsnummer,
        belop = this.belop ?: ZeroBelop,
        transaksjonsId = UUID.randomUUID(),
        utbetalingsUuid = tidligereUtbetaling?.utbetalingsUuid ?: UtbetalingUuid.random(),
        utbetalingStatus = UtbetalingStatus.UTKAST,
        utbetalingTidspunkt = null,
    )
}