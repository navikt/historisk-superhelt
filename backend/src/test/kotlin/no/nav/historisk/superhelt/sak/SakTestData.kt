package no.nav.historisk.superhelt.sak

import net.datafaker.Faker
import no.nav.historisk.superhelt.sak.db.SakJpaEntity
import no.nav.person.Fnr
import java.time.LocalDate
import java.util.concurrent.TimeUnit

object SakTestData {

    val faker: Faker = Faker()

    val sakEntityMinimum = sakEntityMinimum(Fnr(faker.numerify("###########")))

    fun sakUtenUtbetaling() = Sak(
        saksnummer = Saksnummer(faker.numerify("Mock-#####")),
        type = faker.options().option(StonadsType::class.java),
        fnr = Fnr(faker.numerify("###########")),
        tittel = faker.greekPhilosopher().quote(),
        soknadsDato = LocalDate.ofInstant(
            faker.timeAndDate().past(30, TimeUnit.DAYS),
            java.time.ZoneId.systemDefault()
        ),
        status = SakStatus.UNDER_BEHANDLING,
        vedtak = null,
        opprettetDato = faker.timeAndDate().past(1, TimeUnit.DAYS),
        saksbehandler = faker.bothify("???###"),
        utbetaling = null,
        forhandstilsagn = null
    )

    fun sakEntityMinimum(fnr: Fnr): SakJpaEntity {
        return SakJpaEntity(
            type = faker.options().option(StonadsType::class.java),
            fnr = fnr,
            status = SakStatus.UNDER_BEHANDLING,
            saksbehandler = faker.greekPhilosopher().name()
        )
    }

}