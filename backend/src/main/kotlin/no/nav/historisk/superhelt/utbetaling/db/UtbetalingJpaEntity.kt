package no.nav.historisk.superhelt.utbetaling.db

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import no.nav.common.types.Behandlingsnummer
import no.nav.common.types.Belop
import no.nav.helved.KlasseKode
import no.nav.helved.UtbetalingUuid
import no.nav.historisk.superhelt.sak.db.SakJpaEntity
import no.nav.historisk.superhelt.utbetaling.Utbetaling
import no.nav.historisk.superhelt.utbetaling.UtbetalingStatus
import org.hibernate.Hibernate
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "utbetaling")
class UtbetalingJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "utbetaling_id")
    var id: Long? = null,

    @Column(name = "transaksjons_id", nullable = false, unique = true)
    var transaksjonsId: UUID = UUID.randomUUID(),

    @Column(name = "utbetalings_uuid", nullable = false)
    var utbetalingsUuid: UtbetalingUuid,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sak_id", nullable = false)
    val sak: SakJpaEntity,

    @Column(name = "behandlingsnummer", nullable = false)
    var behandlingsnummer: Behandlingsnummer,

    @Enumerated(EnumType.STRING)
    @Column(name = "klassekode", nullable = false)
    var klassekode: KlasseKode,

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

    internal fun toDomain(): Utbetaling {
        return Utbetaling(
            belop = Belop(this.belop),
            klasseKode = this.klassekode,
            saksnummer = this.sak.saksnummer,
            behandlingsnummer = this.behandlingsnummer,
            transaksjonsId = this.transaksjonsId,
            utbetalingsUuid = this.utbetalingsUuid,
            utbetalingStatus = this.utbetalingStatus,
            utbetalingTidspunkt = this.utbetalingTidspunkt,
        )
    }
}
