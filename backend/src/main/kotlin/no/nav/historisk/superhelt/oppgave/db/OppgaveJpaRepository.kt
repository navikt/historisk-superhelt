package no.nav.historisk.superhelt.oppgave.db

import no.nav.common.types.EksternOppgaveId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OppgaveJpaRepository : JpaRepository<OppgaveEntity, Long> {
    fun findByEksternOppgaveId(id: EksternOppgaveId): OppgaveEntity?
    fun findAllBySakId(sakId: Long): List<OppgaveEntity>
}
