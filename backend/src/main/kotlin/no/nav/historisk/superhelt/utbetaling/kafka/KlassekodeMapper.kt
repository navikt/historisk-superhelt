package no.nav.historisk.superhelt.utbetaling.kafka

import no.nav.helved.KlasseKode
import no.nav.historisk.superhelt.sak.StonadsType

internal val StonadsType.klassekode: KlasseKode get() = when (this) {
    StonadsType.PARYKK -> KlasseKode.PARYKK
    StonadsType.ORTOPEDI -> KlasseKode.ORTOPEDISK_PROTESE
    StonadsType.ANSIKT_PROTESE -> KlasseKode.ANSIKTSDEFEKTPROTESE
    StonadsType.OYE_PROTESE -> KlasseKode.ØYEPROTESE
    StonadsType.BRYSTPROTESE -> KlasseKode.BRYSTPROTESE
    StonadsType.FOTTOY -> KlasseKode.VANLIGE_SKO
    StonadsType.REISEUTGIFTER -> KlasseKode.REISEUTGIFTER
//    StonadsType.FOLKEHOYSKOLE -> throw IllegalArgumentException("$this har ikke klassekode i Helved")
//    StonadsType.GRUNNMONSTER -> throw IllegalArgumentException("$this har ikke klassekode i Helved")
}