package no.nav.historisk.superhelt.brev.rest

import no.nav.historisk.superhelt.brev.BrevMottaker
import no.nav.historisk.superhelt.brev.BrevType

data class OpprettBrevRequest(
    val type: BrevType,
    val mottaker: BrevMottaker
)

data class OppdaterBrevRequest(
    val tittel: String?,
    val innhold: String?
)
