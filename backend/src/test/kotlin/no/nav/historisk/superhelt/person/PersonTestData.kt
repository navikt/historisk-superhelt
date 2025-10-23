package no.nav.historisk.superhelt.person

import no.nav.person.Fnr
import no.nav.person.Persondata

object PersonTestData {

    val testPerson = Persondata(
        fnr = Fnr("12345678901"),
        navn = "Ola Nordmann",
        aktorId = "1234567890123",
        alleFnr = setOf(Fnr("12345678901")),
        doedsfall = null,
        verge = null,
        harTilgang = true,
        fornavn = "Ola",
        etternavn = "Nordmann",
        adressebeskyttelseGradering = null,
    )
}