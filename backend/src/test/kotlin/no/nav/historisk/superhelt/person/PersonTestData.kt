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

    val testPersonMedAdressebeskyttelse = Persondata(
        fnr = FolkeregisterIdent("12345678903"),
        navn = "Kjell Aronsen",
        aktorId = AktorId("1234567890125"),
        alleFnr = setOf(FolkeregisterIdent("12345678903")),
        doedsfall = null,
        foedselsdato = LocalDate.parse("1940-04-09"),
        verge = null,
        harTilgang = true,
        fornavn = "Kjell",
        etternavn = "Aronsen",
        adressebeskyttelseGradering = AdressebeskyttelseGradering.STRENGT_FORTROLIG,
    )

    val testPersonMedVerge = Persondata(
        fnr = FolkeregisterIdent("12345678904"),
        navn = "Thomas Andre",
        aktorId = AktorId("1234567890126"),
        alleFnr = setOf(FolkeregisterIdent("12345678904")),
        doedsfall = null,
        foedselsdato = LocalDate.parse("1984-08-20"),
        verge = FolkeregisterIdent("12345678903"),
        harTilgang = true,
        fornavn = "Thomas",
        etternavn = "Andre",
    )
}
