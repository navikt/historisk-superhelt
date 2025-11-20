package no.nav.historisk.superhelt.utbetaling

import net.datafaker.Faker
import java.util.*

object UtbetalingTestData {
    private val faker = Faker()

    fun utbetalingMinimum() = Utbetaling(
        belop = faker.number().positive(),
        uuid = UUID.randomUUID(),
        utbetalingStatus = UtbetalingStatus.UTKAST,
        utbetalingTidspunkt = null
    )
}