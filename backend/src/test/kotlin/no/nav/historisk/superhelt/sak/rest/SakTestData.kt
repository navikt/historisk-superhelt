package no.nav.historisk.superhelt.sak.rest

import net.datafaker.Faker
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.StonadsType
import no.nav.historisk.superhelt.sak.db.SakJpaEntity
import no.nav.person.Fnr

object SakTestData {

    val faker: Faker = Faker()

    val sakEntityMinimum = sakMinumum(Fnr(faker.numerify("###########")))

    fun sakMinumum(fnr: Fnr): SakJpaEntity {
       return SakJpaEntity(
            type = faker.options().option(StonadsType::class.java),
            fnr = fnr,
            status = SakStatus.UNDER_BEHANDLING,
            saksbehandler = faker.greekPhilosopher().name()
        )
    }
}