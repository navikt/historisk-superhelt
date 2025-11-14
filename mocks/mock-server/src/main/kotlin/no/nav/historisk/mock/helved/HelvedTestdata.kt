package no.nav.historisk.mock.helved

import net.datafaker.Faker
import no.nav.helved.*
import java.time.LocalDate

object HelvedTestdata {

    private val faker = Faker()

    fun lagStatusMelding(
        utbetalingId: String,
        status: StatusType,
    ): UtbetalingStatusMelding {


        val melding = UtbetalingStatusMelding(
            status = status,
            detaljer = Detaljer(
                ytelse = "HISTORISK",
                linjer = listOf(
                    Linje(
                        behandlingId = utbetalingId,
                        fom = LocalDate.now(),
                        tom = LocalDate.now(),
                        vedtakssats = 100,
                        beløp = faker.number().numberBetween(1, 9999),
                        klassekode = KlasseKode.HJRIM.name
                    )
                )
            ),
            error = null
        )
        if (status == StatusType.FEILET) {
            return melding.copy(
                error = StatusError(
                    statusCode = 400,
                    msg = faker.famousLastWords().lastWords(),
                    doc = "http/mock/doc/url"
                )
            )
        }
        return melding
    }
}