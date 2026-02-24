package no.nav.historisk.superhelt.brev

enum class BrevStatus() {
    NY,
    UNDER_ARBEID,
    KLAR_TIL_SENDING,
    SENDT, ;

    fun isCompleted(): Boolean {
        return this == KLAR_TIL_SENDING || this == SENDT
    }
}
