package no.nav.historisk.superhelt.oppgave

import no.nav.common.types.EksternOppgaveId
import no.nav.historisk.innsyn.saksbehandling.oppgave.model.OppgaveJpaRepository
import no.nav.historisk.superhelt.sak.Sak

class OppgaveRepository (private val oppgaveJpaRepository: OppgaveJpaRepository){

    fun finnSakForOppgave(oppgaveId: EksternOppgaveId): Sak{
       return oppgaveJpaRepository.findByEksternOppgaveId(oppgaveId).sak.toDomain()
    }



}