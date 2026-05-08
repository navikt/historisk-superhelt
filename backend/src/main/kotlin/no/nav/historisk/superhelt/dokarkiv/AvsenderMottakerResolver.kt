package no.nav.historisk.superhelt.dokarkiv

import no.nav.dokarkiv.AvsenderMottaker
import no.nav.dokarkiv.AvsenderMottakerIdType
import no.nav.historisk.superhelt.brev.BrevMottaker
import no.nav.historisk.superhelt.person.PersonService
import no.nav.historisk.superhelt.sak.Sak
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AvsenderMottakerResolver(private val personService: PersonService) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun resolve(mottakerType: BrevMottaker, sak: Sak): AvsenderMottaker =
        when (mottakerType) {
            BrevMottaker.BRUKER -> {
                val verge = personService.hentVerge(vergetrengendeFnr = sak.fnr)
                if (verge != null) logger.debug("Fant verge for bruker, setter verge som mottaker.")
                val mottakerFnr = verge?.fnr ?: sak.fnr
                AvsenderMottaker(
                    id = mottakerFnr.value,
                    idType = AvsenderMottakerIdType.FNR,
                )
            }
            BrevMottaker.SAMHANDLER -> throw UnsupportedOperationException("Samhandler støttes ikke ennå")
        }
}
