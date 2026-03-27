package no.nav.historisk.mock.infotrygd

import net.datafaker.Faker
import no.nav.infotrygd.InfotrygdHistorikkResponse
import no.nav.infotrygd.PersonkortOversiktsdetalj

val faker = Faker()


fun genererInfotrygdHistorikkResponse(): InfotrygdHistorikkResponse {
    val size = faker.random().nextInt(0, 10)
    return InfotrygdHistorikkResponse(
        personkort = List(size) {
            personkortOversiktsdetalj()
        }
    )
}

private fun personkortOversiktsdetalj(): PersonkortOversiktsdetalj {
    val date = faker.timeAndDate().birthday()
    return PersonkortOversiktsdetalj(
        dato = date,
        fom = date,
        tom = null,
        tekst = faker.chuckNorris().fact().take(30),
        kontonummer = faker.numerify("#######"),
        bevilgetBelop = faker.commerce().price(),
        betaltBelop = null
    )
}
