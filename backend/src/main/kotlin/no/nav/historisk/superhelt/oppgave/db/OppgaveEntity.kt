package no.nav.historisk.superhelt.oppgave.db
import jakarta.persistence.*
import no.nav.common.types.EksternOppgaveId
import no.nav.historisk.superhelt.sak.db.SakJpaEntity
import no.nav.oppgave.OppgaveType

@Entity
@Table(name = "oppgave")
class OppgaveEntity(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "oppgave_id")
    var id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "sak_id")
    val sak: SakJpaEntity,

    @Column(name = "ekstern_oppgave_id")
    val eksternOppgaveId: EksternOppgaveId,

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    val type: OppgaveType,
)



