package no.nav.historisk.mock.pdl

import net.datafaker.Faker
import no.nav.pdl.*
import no.nav.tilgangsmaskin.Avvisningskode

val faker = Faker()

fun fakeAktoerIdFromFnr(fnr: String): String = "AK" + fnr.take(11)
fun fnrFromAktoerId(aktoerId: String): String = aktoerId.substring(2, 13)

fun pdlData(fnr: String) = PdlData(
    hentPerson = Person(
        navn = listOf(
            Navn(
                fornavn = faker.name().firstName(),
                mellomnavn = "Mock",
                etternavn = faker.name().lastName()
            )
        ),
        //Logikk som gir en sjanse for at personen er død, og i så fall en tilfeldig dødsdato mellom 0 og 80 år etter fødselsdato
        doedsfall = if (faker.number().numberBetween(0, 100) < 20) listOf(Doedsfall(faker.timeAndDate().birthday().plusYears(faker.number().numberBetween(0, 80).toLong()).toString())) else listOf(),
        foedselsdato = listOf(Foedselsdato(faker.timeAndDate().birthday())),
        adressebeskyttelse = listOf(),
        vergemaalEllerFremtidsfullmakt = listOf()
    ),
    hentIdenter = Identliste(
        identer = listOf(
            IdentInformasjon(
                ident = fnr,
                gruppe = IdentGruppe.FOLKEREGISTERIDENT,
                historisk = false
            ),
            IdentInformasjon(
                ident = fakeAktoerIdFromFnr(fnr),
                gruppe = IdentGruppe.AKTORID,
                historisk = false
            ),
        )
    )
)

fun pdlError(avvisningskode: Avvisningskode)= listOf(
    PdlError(
        message = avvisningskode.name,
        locations = listOf(),
        path = listOf(),
        extensions = PdlErrorExtension(
            code = avvisningskode.toPdlKode(),
            classification = "ExecutionAborted"
        )
    )
)

private fun Avvisningskode.toPdlKode(): String? {
    return when (this) {
        Avvisningskode.UKJENT_PERSON -> PdlFeilkoder.NOT_FOUND
        else ->  PdlFeilkoder.UNAUTHORIZED
    }
}
