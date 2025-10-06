package no.nav.historisk.superhelt.person

import no.nav.pdl.PdlClient
import no.nav.person.Fnr
import no.nav.person.PdlPersondataParser
import no.nav.person.Persondata
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class PersonService(
    private val pdlClient: PdlClient,
) {
    private val pdlParser = PdlPersondataParser()

    @PreAuthorize("@tilgangsmaskin.harTilgang(#fnr)")
    // TODO caching?
    fun hentPerson(fnr: Fnr): Persondata {
        val pdlResponse = pdlClient.getPersonOgIdenter(ident = fnr)
        return pdlParser.parsePdlResponse(pdlResponse)
    }

    fun maskerFnr(fnr: Fnr): String {
        //TODO fix maskering
        return fnr.reversed()
    }
}

