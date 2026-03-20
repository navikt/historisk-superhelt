package no.nav.historisk.superhelt.vedtak

enum class VedtaksResultat(val navn: String) {
    INNVILGET("Innvilget"),
    DELVIS_INNVILGET("Delvis innvilget"),
    AVSLATT("Avslått"),

    /** Saken er henlagt fordi bruker f.eks. trekker søknad. */
    HENLAGT("Henlagt") ;

    fun isInnvilget(): Boolean {
        return this == INNVILGET || this == DELVIS_INNVILGET
    }

}
