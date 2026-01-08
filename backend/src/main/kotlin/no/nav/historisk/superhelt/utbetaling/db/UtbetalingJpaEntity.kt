package no.nav.historisk.superhelt.utbetaling.db

import jakarta.persistence.*
import no.nav.common.types.Belop
import no.nav.historisk.superhelt.sak.db.SakJpaEntity
import no.nav.historisk.superhelt.utbetaling.Utbetaling
import no.nav.historisk.superhelt.utbetaling.UtbetalingStatus
import org.hibernate.Hibernate
import java.time.Instant
import java.util.*

@Entity
@Table(name = "utbetaling")
class UtbetalingJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "utbetaling_id")
    var id: Long? = null,

    @Column(name = "utbetaling_uuid", nullable = false, unique = true)
    var uuid: UUID = UUID.randomUUID(),

    @OneToOne(mappedBy = "utbetaling")
    val sak: SakJpaEntity,

    var belop: Int,

    var utbetalingTidspunkt: Instant? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "utbetaling_status")
    var utbetalingStatus: UtbetalingStatus = UtbetalingStatus.UTKAST
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as UtbetalingJpaEntity

        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    internal fun toDomain(): Utbetaling? {
        return Utbetaling(
            belop = Belop(this.belop),
            saksnummer = this.sak.saksnummer,
            uuid = this.uuid,
            utbetalingStatus = this.utbetalingStatus,
            utbetalingTidspunkt = this.utbetalingTidspunkt

        )
    }
}
