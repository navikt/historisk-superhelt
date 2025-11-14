package no.nav.historisk.superhelt.sak

enum class SakRettighet {
    LES,

    /** Gir rettighet til å endre sakens innhold og fatte vedtak*/
    SAKSBEHANDLE,

    /** Gir rettighet til å attestere og ferdigstille sak */
    FERDIGSTILLE,

    /** Gir rettighet til å gjenåpne en ferdigstilt sak */
    GJENAPNE,
}
