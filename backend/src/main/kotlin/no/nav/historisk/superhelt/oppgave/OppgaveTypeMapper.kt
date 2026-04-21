package no.nav.historisk.superhelt.oppgave

import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.oppgave.OppgaveType

object OppgaveTypeMapper {

    fun fromSakstatus(sakstatus: SakStatus): OppgaveType? {
        return when (sakstatus) {
            SakStatus.UNDER_BEHANDLING -> OppgaveType.BEH_SAK
            SakStatus.TIL_ATTESTERING -> OppgaveType.GOD_VED
            SakStatus.FERDIG_ATTESTERT -> OppgaveType.GOD_VED
            else -> null
        }
    }
}
