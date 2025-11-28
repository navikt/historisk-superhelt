package no.nav.historisk.superhelt.brev

import java.util.*


data class BrevUtkast(
    val uuid: UUID,
    val tittel: String?,
    /** html innholdet i brevet */
    val innhold: String?,
    val type: BrevType,
    val mottakerType: BrevMottaker,
    val status: BrevStatus = BrevStatus.NY
)

