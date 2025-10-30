package no.nav.historisk.superhelt.sak.db

import jakarta.persistence.*
import no.nav.historisk.superhelt.sak.Forhandstilsagn
import org.hibernate.Hibernate
import java.time.Instant

@Entity
@Table(name = "forhanstilsagn")
//@EntityListeners(AuditingEntityListener::class)
class ForhandsTilsagnJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

//    @CreatedDate
    val createdDate: Instant = Instant.now(),

    @OneToOne(mappedBy = "forhandstilsagn")
    val sak: SakJpaEntity,

    ) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as ForhandsTilsagnJpaEntity

        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    internal fun toDomain(): Forhandstilsagn ? {
        return Forhandstilsagn(
        )
    }

}


