package no.nav.oppgave

import no.nav.oppgave.model.OppgaveDto


/**
 * Oppgavetyper for Tema HEL og HJE hentet fra /api/v1/kodeverk/oppgavetype/{tema}
 */

enum class OppgaveType(val oppgavetype: String, val beskrivelse: String) {

    // Felles for HEL og HJE
    BEH_SED("BEH_SED", "Behandle SED"),
    BEH_SAK("BEH_SAK", "Behandle sak"),
    BEH_SAK_MK("BEH_SAK_MK", "Behandle sak (Manuell)"),
    BEH_UND_VED("BEH_UND_VED", "Behandle underkjent vedtak"),
    FDR("FDR", "Fordeling"),
    GOD_VED("GOD_VED", "Godkjenne vedtak"),
    INNH_DOK("INNH_DOK", "Innhent dokumentasjon"),
    JFR("JFR", "Journalføring"),
    KON_UTG_SCA_DOK("KON_UTG_SCA_DOK", "Kontroller utgående skannet dokument"),
    KONT_BRUK("KONT_BRUK", "Kontakt bruker"),
    RETUR("RETUR", "Behandle returpost"),
    SVAR_IK_MOT("SVAR_IK_MOT", "Svar ikke mottatt"),
    VUR("VUR", "Vurder dokument"),
    VUR_KONS_YTE("VUR_KONS_YTE", "Vurder konsekvens for ytelse"),
    VUR_SVAR("VUR_SVAR", "Vurder svar"),
    VURD_BREV("VURD_BREV", "Vurder brev"),
    VURD_HENV("VURD_HENV", "Vurder henvendelse"),
    VURD_NOTAT("VURD_NOTAT", "Vurder notat"),

    // Spesifikke for HJE
    FLY("FLY", "Flyttesak"),
    HJELP_UTPROV("HJELP_UTPROV", "Hjelp til utprøving"),
    MOTK("MOTK", "Krav mottatt"),
    ROB_BEH("ROB_BEH", "Robotbehandling"),

    // Spesialverdi for fallback
    UKJENT("", "Ukjent oppgavetype")
    ;

}

val OppgaveDto.type: OppgaveType get() = OppgaveType.entries.find<OppgaveType> { it.oppgavetype == oppgavetype } ?: OppgaveType.UKJENT
