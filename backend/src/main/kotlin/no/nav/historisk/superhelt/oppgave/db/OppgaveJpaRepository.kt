package no.nav.historisk.innsyn.saksbehandling.oppgave.model

import no.nav.common.types.EksternOppgaveId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OppgaveJpaRepository : JpaRepository<OppgaveEntity, Long> {
    fun findByEksternOppgaveId(id: EksternOppgaveId): OppgaveEntity
    fun findAllBySakId(sakId: Long): List<OppgaveEntity>
}
