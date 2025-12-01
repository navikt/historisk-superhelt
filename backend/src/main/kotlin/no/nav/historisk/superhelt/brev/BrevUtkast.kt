package no.nav.historisk.superhelt.brev


data class BrevUtkast(
    val uuid: BrevId,
    val tittel: String?,
    /** html innholdet i brevet */
    val innhold: String?,
    val type: BrevType,
    val mottakerType: BrevMottaker,
    val status: BrevStatus = BrevStatus.NY
)

