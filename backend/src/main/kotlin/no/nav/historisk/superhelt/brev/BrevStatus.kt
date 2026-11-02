package no.nav.historisk.superhelt.brev

enum class BrevStatus() {
    UTKAST,
    KLAR_TIL_SENDING,
    ARKIVERT,
    SENDT, ;

    fun isSendt(): Boolean {
        return this != UTKAST
    }

    fun tillattNesteStatus(nesteStatus: BrevStatus): Boolean {
        if (nesteStatus == this) {
            return true
        }
        return when (this) {
            UTKAST -> nesteStatus == KLAR_TIL_SENDING
            KLAR_TIL_SENDING -> nesteStatus == ARKIVERT
            ARKIVERT -> nesteStatus == SENDT
            SENDT -> false
        }
    }
}
