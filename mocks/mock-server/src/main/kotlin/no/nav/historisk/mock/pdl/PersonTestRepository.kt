package no.nav.historisk.mock.pdl

import no.nav.pdl.PdlData
import no.nav.tilgangsmaskin.Avvisningskode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PersonTestRepository {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val repository = mutableMapOf<String, TestPerson>()
    
    init {
        // Person
        generateAndCacheResponse("40400000000", Avvisningskode.UKJENT_PERSON)

        // Noen personer med avvisningskoder
        generateAndCacheResponse("40300000001", Avvisningskode.AVVIST_HABILITET)
        generateAndCacheResponse("40300000002", Avvisningskode.AVVIST_AVDØD)
        generateAndCacheResponse("40300000006", Avvisningskode.AVVIST_STRENGT_FORTROLIG_ADRESSE)
        generateAndCacheResponse("40300000007", Avvisningskode.AVVIST_FORTROLIG_ADRESSE)

        // Noen personer med hemmelig adresse som blir godtatt av tilgangasmaskin
        //TODO
    }


    fun lagre( data: TestPerson): String {
        logger.debug("Lagrer Person {} med data {}", data.fnr, data)
        repository[data.fnr] = data
        return data.fnr
    }

    fun findOrCreate(fnr: String): TestPerson {
        return repository[fnr]
            ?: generateAndCacheResponse(fnr)
    }

    private fun generateAndCacheResponse(fnr: String, avvisningskode: Avvisningskode?= null): TestPerson {

        val data = pdlData(fnr)
        val testPerson = TestPerson(fnr = fnr, data = data, avvisningskode)
        lagre( testPerson)
        logger.debug("Registerer ny person for pdl: {} -> {}", fnr, data)
        return testPerson
    }

    fun getAll(): Map<String, TestPerson> {
        return repository
    }

    data class TestPerson(val fnr: String, val data: PdlData, val avisningskode: Avvisningskode?=null)
}