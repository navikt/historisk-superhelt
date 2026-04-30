package no.nav.historisk.superhelt.oppgave

import no.nav.historisk.superhelt.StonadsType
import no.nav.oppgave.Behandlingstema
import no.nav.oppgave.Behandlingstype

/** Tema og type for hva oppgaven gjelder, satt sammen av behandlingstema og behandlingstype fra oppgaven
 *
 * */
enum class OppgaveGjelder(
    val tema: Behandlingstema?,
    val type: Behandlingstype?,
) {

    ANSIKTSDEFEKTSPROTESE(Behandlingstema.ANSIKTSDEFEKTSPROTESE, null),
    BRYSTPROTESE_PROTESEBH(Behandlingstema.BRYSTPROTESE_PROTESEBH, null),
    ORTOPEDISKE_HJELPEMIDLER(Behandlingstema.ORTOPEDISKE_HJELPEMIDLER, null),
    OYEPROTESE(Behandlingstema.OYEPROTESE, null),
    PARYKK_HODEPLAGG(Behandlingstema.PARYKK_HODEPLAGG, null),
    REISEUTGIFTER(Behandlingstema.REISEUTGIFTER, null),

    ARBEID_UTDANNING(Behandlingstema.ARBEIDS_OG_UTDANNINGSREISER, null)
    ;

    val behandlingstema: String? get() = tema?.kode
    val behandlingstype: String? get() = type?.kode
}

fun StonadsType.tilOppgaveGjelder(): OppgaveGjelder =
    when (this) {
        StonadsType.PARYKK -> OppgaveGjelder.PARYKK_HODEPLAGG
        StonadsType.PROTESE, StonadsType.SPESIALSKO,
        StonadsType.ORTOSE, StonadsType.FOTSENG -> OppgaveGjelder.ORTOPEDISKE_HJELPEMIDLER

        StonadsType.ANSIKT_PROTESE -> OppgaveGjelder.ANSIKTSDEFEKTSPROTESE
        StonadsType.OYE_PROTESE -> OppgaveGjelder.OYEPROTESE
        StonadsType.BRYSTPROTESE -> OppgaveGjelder.BRYSTPROTESE_PROTESEBH
        StonadsType.FOTTOY -> OppgaveGjelder.ORTOPEDISKE_HJELPEMIDLER
        StonadsType.REISEUTGIFTER -> OppgaveGjelder.REISEUTGIFTER

        StonadsType.ARBEID_UTDANNING -> OppgaveGjelder.ARBEID_UTDANNING
    }
