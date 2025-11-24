package no.nav.historisk.superhelt.utbetaling.db

import jakarta.persistence.*
import no.nav.common.types.Belop
import no.nav.historisk.superhelt.sak.db.SakJpaEntity
import no.nav.historisk.superhelt.utbetaling.Forhandstilsagn
import org.hibernate.Hibernate
import java.time.Instant
import java.util.*

@Entity
@Table(name = "forhandtilsagn")
class ForhandTilsagnJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    var uuid: UUID = UUID.randomUUID(),
    val createdDate: Instant = Instant.now(),
    @OneToOne(mappedBy = "forhandstilsagn") val sak: SakJpaEntity,
    var belop: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as ForhandTilsagnJpaEntity

        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    internal fun toDomain(): Forhandstilsagn? {
        return Forhandstilsagn(belop = Belop(this.belop))
    }
}
