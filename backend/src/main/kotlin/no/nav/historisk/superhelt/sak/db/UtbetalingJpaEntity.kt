package no.nav.historisk.superhelt.sak.db

import jakarta.persistence.*
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.StonadsType
import no.nav.historisk.superhelt.sak.VedtakType
import no.nav.historisk.superhelt.utbetaling.Utbetaling
import no.nav.person.Fnr
import org.hibernate.Hibernate
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "utbetaling")
//@EntityListeners(AuditingEntityListener::class)
class UtbetalingJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
//
//    @CreatedDate
//    var createdDate: Instant? = null,
//
//    @CreatedBy
//    var createdBy: String? = null,
//
//    @LastModifiedDate
//    var lastModifiedDate: Instant? = null,
//
//    @LastModifiedBy
//    var lastModifiedBy: String? = null,

    @OneToOne(mappedBy = "utbetaling")
    val sak: SakJpaEntity,

    var belop: Double,

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as UtbetalingJpaEntity

        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

}

internal fun UtbetalingJpaEntity?.toDomain(): Utbetaling? {
    if (this == null) return null

    return Utbetaling(
        belop = this.belop,
        bruker = this.sak.fnr
    )
}
