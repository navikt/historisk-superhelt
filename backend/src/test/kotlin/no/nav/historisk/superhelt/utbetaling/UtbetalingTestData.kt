package no.nav.historisk.superhelt.utbetaling

import net.datafaker.Faker
import no.nav.common.types.NorskeKroner
import java.util.*

object UtbetalingTestData {
    private val faker = Faker()

    fun utbetalingMinimum() = Utbetaling(
        belop = NorskeKroner(faker.number().positive()),
        uuid = UUID.randomUUID(),
        utbetalingStatus = UtbetalingStatus.UTKAST,
        utbetalingTidspunkt = null
    )
}