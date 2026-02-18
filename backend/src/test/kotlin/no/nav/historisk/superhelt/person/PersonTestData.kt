package no.nav.historisk.superhelt.person

import no.nav.common.types.AktorId
import no.nav.common.types.FolkeregisterIdent
import no.nav.person.Persondata
import java.time.LocalDate
import no.nav.pdl.AdressebeskyttelseGradering

object PersonTestData {

    val testPerson = Persondata(
        fnr = FolkeregisterIdent("12345678901"),
        navn = "Ola Nordmann",
        aktorId = AktorId("1234567890123"),
        alleFnr = setOf(FolkeregisterIdent("12345678901")),
        doedsfall = null,
        foedselsdato = LocalDate.now().minusYears(30),
        verge = null,
        harTilgang = true,
        fornavn = "Ola",
        etternavn = "Nordmann",
        adressebeskyttelseGradering = null,
    )

    val testPersonDoed = Persondata(
        fnr = FolkeregisterIdent("12345678902"),
        navn = "Oline Nordmann",
        aktorId = AktorId("1234567890124"),
        alleFnr = setOf(FolkeregisterIdent("12345678902")),
        doedsfall = "2025-01-15",
        foedselsdato = LocalDate.now().minusYears(50),
        verge = null,
        harTilgang = true,
        fornavn = "Oline",
        etternavn = "Nordmann",
        adressebeskyttelseGradering = null,
    )
}