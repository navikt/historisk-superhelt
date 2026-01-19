package no.nav.oppgave

import no.nav.oppgave.model.OppgaveDto


/**
 * Oppgavetyper for Tema HEL hentet fra /api/v1/kodeverk/oppgavetype/{tema}
 */

enum class OppgaveTypeTemaHel(val oppgavetype: String, val term: String) {
    BEH_SED("BEH_SED", "Behandle SED"),
    VURD_NOTAT("VURD_NOTAT", "Vurder notat"),
    VURD_BREV("VURD_BREV", "Vurder brev"),
    BEH_SAK("BEH_SAK", "Behandle sak"),
    BEH_SAK_MK("BEH_SAK_MK", "Behandle sak (Manuell)"),
    INNH_DOK("INNH_DOK", "Innhent dokumentasjon"),
    JFR("JFR", "Journalføring"),
    KON_UTG_SCA_DOK("KON_UTG_SCA_DOK", "Kontroller utgående skannet dokument"),
    KONT_BRUK("KONT_BRUK", "Kontakt bruker"),
    RETUR("RETUR", "Behandle returpost"),
    SVAR_IK_MOT("SVAR_IK_MOT", "Svar ikke mottatt"),
    VUR("VUR", "Vurder dokument"),
    VUR_KONS_YTE("VUR_KONS_YTE", "Vurder konsekvens for ytelse"),
    VUR_SVAR("VUR_SVAR", "Vurder svar"),
    VURD_HENV("VURD_HENV", "Vurder henvendelse"),
    BEH_AVV_ADR("BEH_AVV_ADR", "Behandle avvist adresse"),
    FDR("FDR", "Fordeling"),
    GOD_VED("GOD_VED", "Godkjenne vedtak"),
    BEH_UND_VED("BEH_UND_VED", "Behandle underkjent vedtak"),
    UKJENT("", "Ukjent oppgavetype")
    ;

}

val OppgaveDto.type: OppgaveTypeTemaHel get() = OppgaveTypeTemaHel.entries.find<OppgaveTypeTemaHel> { it.oppgavetype == oppgavetype } ?: OppgaveTypeTemaHel.UKJENT
