package no.nav.historisk.superhelt.utbetaling.db

import jakarta.persistence.*
import java.time.Instant
import java.util.*
import no.nav.historisk.superhelt.sak.db.SakJpaEntity
import no.nav.historisk.superhelt.utbetaling.Utbetaling
import org.hibernate.Hibernate

@Entity
@Table(name = "utbetaling")
class UtbetalingJpaEntity(
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
  var uuid: UUID = UUID.randomUUID(),
  @OneToOne(mappedBy = "utbetaling") val sak: SakJpaEntity,
  var belop: Int,
  var utbetalingTidspunkt: Instant? = null,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as UtbetalingJpaEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  internal fun toDomain(): Utbetaling? {
    return Utbetaling(belop = this.belop)
  }
}
