package no.nav.historisk.superhelt.vedtak

enum class VedtaksResultat {
    INNVILGET,
    DELVIS_INNVILGET,
    AVSLATT,

    /** Saken er henlagt fordi bruker feks trekker søknad. */
    HENLAGT,

    /** Nav har gjort noe feil. Saken avvises  */
    FEILREGISTRERT;
}