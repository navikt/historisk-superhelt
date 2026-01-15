package no.nav.historisk.superhelt.person

import no.nav.common.types.FolkeregisterIdent
import no.nav.historisk.superhelt.infrastruktur.getCurrentNavIdent
import no.nav.pdl.PdlClient
import no.nav.person.PdlPersondataParser
import no.nav.person.Persondata
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service

@Service
class PersonService(
    private val pdlClient: PdlClient,
    cacheManager: CacheManager
) {
    private val pdlParser = PdlPersondataParser()
    private val cache = cacheManager.getCache("pdlCache")
        ?: throw IllegalStateException("Cache 'pdlCache' not found")


    fun hentPerson(fnr: FolkeregisterIdent): Persondata? {
        val cacheKey = "${getCurrentNavIdent()}:${fnr.value}"

        return cache.get(cacheKey, Persondata::class.java)
            ?: hentFraPdl(fnr).also { result ->
                cache.put(cacheKey, result)
            }
    }

    private fun hentFraPdl(fnr: FolkeregisterIdent): Persondata? {
        val pdlResponse = pdlClient.getPersonOgIdenter(ident = fnr.value)
        val persondata = pdlParser.parsePdlResponse(pdlResponse)
        return persondata
    }

}

