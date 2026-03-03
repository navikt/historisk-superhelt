package no.nav.historisk.superhelt.utbetaling

import net.datafaker.Faker
import no.nav.common.types.Behandlingsnummer
import no.nav.common.types.Belop
import no.nav.common.types.Saksnummer
import no.nav.helved.UtbetalingUuid
import java.util.*

object UtbetalingTestData {
    private val faker = Faker()

    fun utbetalingMinimum(belop: Int? = null) = Utbetaling(
        belop = Belop(belop ?: faker.number().positive()),
        transaksjonsId = UUID.randomUUID(),
        utbetalingsUuid = UtbetalingUuid(UUID.randomUUID()),
        utbetalingStatus = UtbetalingStatus.UTKAST,
        utbetalingTidspunkt = null,
        saksnummer = Saksnummer(faker.number().positive().toLong()),
        behandlingsnummer = Behandlingsnummer(1),
    )

}