package no.nav.historisk.superhelt.sak

import net.datafaker.Faker
import no.nav.common.types.Fnr
import no.nav.historisk.superhelt.sak.db.SakJpaEntity
import no.nav.historisk.superhelt.utbetaling.UtbetalingTestData
import java.time.LocalDate
import java.util.concurrent.TimeUnit

object SakTestData {

    private val faker: Faker = Faker()

    fun sakUtenUtbetaling(): Sak {
        val saksnummer = faker.numerify("Mock-#####")
        return Sak(
            saksnummer = Saksnummer(saksnummer),
            type = faker.options().option(StonadsType::class.java),
            fnr = Fnr(faker.numerify("###########")),
            tittel = faker.greekPhilosopher().quote(),
            soknadsDato = LocalDate.ofInstant(
                faker.timeAndDate().past(30, TimeUnit.DAYS),
                java.time.ZoneId.systemDefault()
            ),
            status = SakStatus.UNDER_BEHANDLING,
            vedtak = faker.options().option(VedtakType::class.java),
            opprettetDato = faker.timeAndDate().past(1, TimeUnit.DAYS),
            saksbehandler = faker.bothify("???###"),
            utbetaling = null,
            forhandstilsagn = null
        )
    }

    fun sakMedUtbetaling() = sakUtenUtbetaling().copy(
        utbetaling = UtbetalingTestData.utbetalingMinimum()
    )

    fun sakEntityMinimum(fnr: Fnr = Fnr(faker.numerify("###########"))): SakJpaEntity {
        return SakJpaEntity(
            type = faker.options().option(StonadsType::class.java),
            fnr = fnr,
            status = SakStatus.UNDER_BEHANDLING,
            saksbehandler = faker.greekPhilosopher().name()
        )
    }

}