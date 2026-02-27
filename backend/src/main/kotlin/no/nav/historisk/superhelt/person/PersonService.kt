package no.nav.historisk.superhelt.person

import no.nav.common.types.FolkeregisterIdent
import no.nav.historisk.superhelt.infrastruktur.authentication.getAuthenticatedUser
import no.nav.pdl.PdlClient
import no.nav.person.PdlPersondataParser
import no.nav.person.Persondata
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.cache.get
import org.springframework.stereotype.Service

@Service
class PersonService(
    private val pdlClient: PdlClient,
    cacheManager: CacheManager
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val pdlParser = PdlPersondataParser()
    private val cache = cacheManager.getCache("pdlCache")
        ?: throw IllegalStateException("Cache 'pdlCache' not found")


    fun hentPerson(fnr: FolkeregisterIdent): Persondata? {
        val cacheKey = "${getAuthenticatedUser().navIdent}:${fnr.value}"

        return cache.get(cacheKey, Persondata::class.java)
            ?: hentFraPdl(fnr).also { result ->
                cache.put(cacheKey, result)
            }
    }
    fun hentVerge(vergetrengende: Persondata): Persondata? {
        val cacheKey = "${getAuthenticatedUser().navIdent}:verge:${vergetrengende.fnr.value}"
        val verge = vergetrengende.verge

        if (verge == null) {
            logger.trace("Person ${vergetrengende.fnr.toMaskertPersonIdent()} har ingen verge")
            return null
        }

        verge.tjenesteomraade?.let { tjenesteområder ->
            if (tjenesteområder.isEmpty()) {
                logger.debug("Verge for person ${vergetrengende.fnr.toMaskertPersonIdent()} har tom tjenesteområdeliste. Henter ikke vergeinfo.")
                return null
            }

            val harGyldigTjenesteområde = tjenesteområder.any { område ->
                område.tjenestevirksomhet?.lowercase() == "nav" &&
                område.tjenesteoppgave?.lowercase() == "hjelpemidler"
            }

            if (!harGyldigTjenesteområde) {
                logger.debug("Verge for person ${vergetrengende.fnr.toMaskertPersonIdent()} har ikke tjenesteområde NAV/HJELPEMIDLER. Henter ikke vergeinfo.")
                return null
            }
        }

        return cache.get<Persondata>(cacheKey)
            ?: verge.motpartsPersonident?.let { hentPerson(FolkeregisterIdent(it)) }.also { result ->
                cache.put(cacheKey, result)
            }
    }

    private fun hentFraPdl(fnr: FolkeregisterIdent): Persondata? {
        val pdlResponse = pdlClient.getPersonOgIdenter(ident = fnr.value)
        val persondata = pdlParser.parsePdlResponse(pdlResponse)
        return persondata
    }

}
