package no.nav.historisk.superhelt.oppgave

import no.nav.common.types.EksternOppgaveId
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.oppgave.db.OppgaveEntity
import no.nav.historisk.superhelt.oppgave.db.OppgaveJpaRepository
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.oppgave.OppgaveType
import org.springframework.stereotype.Repository

@Repository
class OppgaveRepository(
    private val oppgaveJpaRepository: OppgaveJpaRepository,
    private val sakRepository: SakRepository) {

    internal fun finnSakForOppgave(oppgaveId: EksternOppgaveId): Sak? {
        return oppgaveJpaRepository.findByEksternOppgaveId(oppgaveId)?.sak?.toDomain()
    }

    internal fun finnOppgaverForSak(saksnummer: Saksnummer): List<EksternOppgaveId> {
        return oppgaveJpaRepository.findAllBySakId(saksnummer.id).map { it.eksternOppgaveId }
    }

    internal fun save(saksnummer: Saksnummer, oppgaveId: EksternOppgaveId, oppgaveType: OppgaveType) {
        val sakEntity = sakRepository.getSakEntityOrThrow(saksnummer)
        oppgaveJpaRepository.save(
            OppgaveEntity(
                sak = sakEntity,
                eksternOppgaveId = oppgaveId,
                type = oppgaveType
            )
        )
    }


}