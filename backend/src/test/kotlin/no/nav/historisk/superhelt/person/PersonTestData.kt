package no.nav.historisk.superhelt.person

import no.nav.common.types.FolkeregisterIdent
import no.nav.person.Persondata

object PersonTestData {

    val testPerson = Persondata(
        fnr = FolkeregisterIdent("12345678901"),
        navn = "Ola Nordmann",
        aktorId = "1234567890123",
        alleFnr = setOf(FolkeregisterIdent("12345678901")),
        doedsfall = null,
        foedselsdato = null,
        verge = null,
        harTilgang = true,
        fornavn = "Ola",
        etternavn = "Nordmann",
        adressebeskyttelseGradering = null,
    )
}