package no.nav.historisk.superhelt.utbetaling

import no.nav.common.types.Behandlingsnummer
import no.nav.common.types.Belop
import no.nav.common.types.Saksnummer
import no.nav.helved.KlasseKode
import no.nav.helved.UtbetalingUuid
import java.time.Instant
import java.util.UUID

data class Utbetaling(
    val saksnummer: Saksnummer,
    val behandlingsnummer: Behandlingsnummer,
    val klasseKode: KlasseKode,
    val belop: Belop,
    val transaksjonsId: UUID,
    val utbetalingsUuid: UtbetalingUuid,
    val utbetalingStatus: UtbetalingStatus,
    val utbetalingTidspunkt: Instant?) {

    /** Skal utbetaling opphøre/annuleres */
    val annulleres: Boolean
        get() = belop.value <= 0

    /** Aggregert loggId for enklere logging */
    internal val loggId= "sak:$saksnummer-$behandlingsnummer, id:$utbetalingsUuid, transaksjon:$transaksjonsId"
}



