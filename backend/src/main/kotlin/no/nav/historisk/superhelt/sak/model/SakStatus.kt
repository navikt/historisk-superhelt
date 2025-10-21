package no.nav.historisk.superhelt.sak.model


enum class SakStatus(val finalState: Boolean) {
    UNDER_BEHANDLING(finalState = false),
    TIL_ATTESTERING(finalState = false),
    INNVILGET(finalState = true),
    DELVIS_INNVILGET(finalState = true),
    AVSLATT(finalState = true),
    HENLAGT(finalState = true),
    AVVIST(finalState = true);
}