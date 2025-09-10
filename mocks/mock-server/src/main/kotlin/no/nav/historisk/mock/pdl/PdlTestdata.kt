package no.nav.historisk.mock.pdl

import com.jayway.jsonpath.JsonPath
import net.datafaker.Faker

val faker = Faker()

fun generatePdlTestdata(ident: String): String {
    val response = JsonPath.parse(pdlResponse)
        .set("$..identer[?(@.gruppe == 'FOLKEREGISTERIDENT' )].ident", ident)
        .set("$..identer[?(@.gruppe == 'AKTORID' )].ident", fakeAktoerIdFromFnr(ident))
        .set("$..fornavn", faker.name().firstName())
        .set("$..mellomnavn", "Mock")
        .set("$..etternavn", faker.name().lastName())
        .jsonString()

    return response
}

fun fakeAktoerIdFromFnr(fnr: String): String = "AK" + fnr.take(11)
fun fnrFromAktoerId(aktoerId: String): String = aktoerId.substring(2, 13)


val pdlResponse = classpathString("/graphql/pdl/respons_dev.json")