package no.nav.historisk.superhelt.person

import no.nav.pdl.PdlClient
import no.nav.person.Fnr
import no.nav.person.PdlPersondataParser
import no.nav.person.Persondata
import org.springframework.stereotype.Service

@Service
class PersonService(
    private val pdlClient: PdlClient,
) {
    private val pdlParser = PdlPersondataParser()

    // TODO caching?
    fun hentPerson(fnr: Fnr): Persondata? {
        val pdlResponse = pdlClient.getPersonOgIdenter(ident = fnr)
        val persondata = pdlParser.parsePdlResponse(pdlResponse)

        return persondata
    }

    fun maskerFnr(fnr: Fnr): String {
        //TODO fix maskering
        return fnr.reversed()
    }

    fun decodeMaskertFnr(maskertPersonident: String): Fnr {
        //TODO fix dekoding
        return maskertPersonident.reversed()
    }
}

