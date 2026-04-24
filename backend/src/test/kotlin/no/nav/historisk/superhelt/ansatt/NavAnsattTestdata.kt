package no.nav.historisk.superhelt.ansatt

import net.datafaker.Faker
import no.nav.common.types.Enhetsnummer
import no.nav.entraproxy.Enhet

object NavAnsattTestdata {
    private val faker: Faker = Faker()

    fun enhet(): Enhet {
        return Enhet(
            enhetnummer = Enhetsnummer( faker.number().digits(4)),
            navn = faker.company().name(),
        )
    }

    fun createEnheter(count: Int= 1): List<Enhet> {
        return (1..count).map { enhet() }.toList()
    }

}
