package no.nav.historisk.superhelt.brev

enum class BrevStatus(val editable: Boolean= true) {
    NY,
    UNDER_ARBEID,
    KLAR_TIL_SENDING(false),
    SENDT(false),
}
