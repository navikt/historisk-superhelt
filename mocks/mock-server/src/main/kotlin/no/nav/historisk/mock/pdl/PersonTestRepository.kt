package no.nav.historisk.mock.pdl

import no.nav.pdl.Adressebeskyttelse
import no.nav.pdl.AdressebeskyttelseGradering
import no.nav.pdl.Doedsfall
import no.nav.pdl.PdlData
import no.nav.pdl.VergeEllerFullmektig
import no.nav.pdl.VergemaalEllerFremtidsfullmakt
import no.nav.tilgangsmaskin.Avvisningskode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PersonTestRepository {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val repository = mutableMapOf<String, TestPerson>()

    init {
        // Person
        generateAndCacheResponse(fnr = "40400000000", tilgangsMaskinAvvisningskode = Avvisningskode.UKJENT_PERSON)

        // Noen personer med avvisningskoder som avvises av tilgangmaskin
        generateAndCacheResponse(fnr = "40300000001", tilgangsMaskinAvvisningskode = Avvisningskode.AVVIST_HABILITET)
        generateAndCacheResponse(fnr = "40300000002", tilgangsMaskinAvvisningskode = Avvisningskode.AVVIST_AVDØD)
        generateAndCacheResponse(
            fnr = "40300000006",
            tilgangsMaskinAvvisningskode = Avvisningskode.AVVIST_STRENGT_FORTROLIG_ADRESSE
        )
        generateAndCacheResponse(
            fnr = "40300000007",
            tilgangsMaskinAvvisningskode = Avvisningskode.AVVIST_FORTROLIG_ADRESSE
        )

        // Noen personer med hemmelig adresse som blir godtatt av tilgangasmaskin
        generateAndCacheResponse(
            fnr = "60000000001",
            data = pdlData(
                "60000000001", adressebeskyttelse = listOf(
                    Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG)
                )
            )
        )
        generateAndCacheResponse(
            fnr = "60000000002",
            data = pdlData(
                "60000000002", adressebeskyttelse = listOf(
                    Adressebeskyttelse(AdressebeskyttelseGradering.FORTROLIG)
                )
            )
        )
        generateAndCacheResponse(
            fnr = "60000000003",
            data = pdlData(
                "60000000003", adressebeskyttelse = listOf(
                    Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND)
                )
            )
        )

        // Person med dødsfall
        generateAndCacheResponse(
            fnr = "70000000001",
            data = pdlData(
                fnr = "70000000001",
                doedsfall = listOf(Doedsfall(doedsdato = "2023-06-15"))
            )
        )
        // Person med vergemål
        generateAndCacheResponse(
            fnr = "70000000002",
            data = pdlData(
                fnr = "70000000002",
                doedsfall = listOf(Doedsfall(doedsdato = "2025-12-12")),
                adressebeskyttelse = listOf(Adressebeskyttelse(AdressebeskyttelseGradering.FORTROLIG)),
                vergemaal = listOf(
                    VergemaalEllerFremtidsfullmakt(VergeEllerFullmektig(motpartsPersonident = "70000000003"))
                )
            )
        )
        // Person som er verge for 70000000002
        generateAndCacheResponse(
            fnr = "70000000003",
            data = pdlData(
                fnr = "70000000003",
            )
        )
    }


    fun lagre(data: TestPerson): String {
        logger.debug("Lagrer Person {} med data {}", data.fnr, data)
        repository[data.fnr] = data
        return data.fnr
    }

    fun findOrCreate(fnr: String): TestPerson {
        return repository[fnr]
            ?: generateAndCacheResponse(fnr)
    }

    private fun generateAndCacheResponse(
        fnr: String,
        tilgangsMaskinAvvisningskode: Avvisningskode? = null,
        data: PdlData = pdlData(fnr)): TestPerson {
        require(fnr.length == 11) { "fnr må være 11 siffer" }
        val testPerson = TestPerson(fnr = fnr, data = data, avvisningskode = tilgangsMaskinAvvisningskode)
        lagre(testPerson)
        logger.debug("Registerer ny person for pdl: {} -> {}", fnr, data)
        return testPerson
    }


    fun getAll(): Map<String, TestPerson> {
        return repository
    }

    data class TestPerson(val fnr: String, val data: PdlData, val avvisningskode: Avvisningskode? = null)
}
