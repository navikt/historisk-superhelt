package no.nav.historisk.superhelt.utbetaling

import no.nav.common.types.Belop.Companion.ZeroBelop
import no.nav.helved.UtbetalingUuid
import no.nav.historisk.superhelt.sak.Sak
import java.time.Instant
import java.util.*

object UtbetalingSakExtentions {

    fun Sak.newUtbetaling(tidligereUtbetaling: Utbetaling? = null) = Utbetaling(
        saksnummer = this.saksnummer,
        behandlingsnummer = this.behandlingsnummer,
        belop = this.belop ?: ZeroBelop,
        transaksjonsId = UUID.randomUUID(),
        utbetalingsUuid = tidligereUtbetaling?.utbetalingsUuid ?: UtbetalingUuid.random(),
        utbetalingStatus = UtbetalingStatus.UTKAST,
        utbetalingTidspunkt = Instant.now(),
    )
}