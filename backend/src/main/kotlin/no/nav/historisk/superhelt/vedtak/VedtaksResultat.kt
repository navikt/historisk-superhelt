package no.nav.historisk.superhelt.vedtak

enum class VedtaksResultat(val navn: String) {
    INNVILGET("innvilget"),
    DELVIS_INNVILGET("delvis innvilget"),
    AVSLATT("avlått"),

    /** Saken er henlagt fordi bruker f.eks. trekker søknad. */
    HENLAGT("henlagt"),

    /** Nav har gjort noe feil. Saken avvises  */
    FEILREGISTRERT("feilregistrert");
}