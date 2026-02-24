package no.nav.historisk.superhelt.brev

typealias BrevList = List<Brev>


fun BrevList.finnGjeldendeBrev(type: BrevType, mottaker: BrevMottaker): Brev? {
    return this
        .sortedByDescending { it.opprettetTidspunkt }
        .find { it.type == type && it.mottakerType == mottaker }
}
