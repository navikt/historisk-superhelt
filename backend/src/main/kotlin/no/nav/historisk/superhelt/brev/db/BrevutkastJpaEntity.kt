package no.nav.historisk.superhelt.brev.db

import jakarta.persistence.*
import no.nav.historisk.superhelt.brev.BrevMottaker
import no.nav.historisk.superhelt.brev.BrevStatus
import no.nav.historisk.superhelt.brev.BrevType
import no.nav.historisk.superhelt.brev.BrevUtkast
import no.nav.historisk.superhelt.sak.db.SakJpaEntity
import org.hibernate.Hibernate
import java.time.Instant
import java.util.*

@Entity
@Table(name = "brevutkast")
class BrevutkastJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var uuid: UUID = UUID.randomUUID(),

    val createdDate: Instant = Instant.now(),

    @ManyToOne
    @JoinColumn(name = "brev_id")
    val sak: SakJpaEntity,

    var tittel: String?,
    var innhold: String?,

    @Enumerated(EnumType.STRING)
    val type: BrevType,

    @Enumerated(EnumType.STRING)
    val mottakerType: BrevMottaker,

    @Enumerated(EnumType.STRING)
    val status: BrevStatus = BrevStatus.NY

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as BrevutkastJpaEntity

        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    internal fun toDomain(): BrevUtkast {
        return BrevUtkast(
            uuid = this.uuid,
            tittel = this.tittel,
            innhold = this.innhold,
            type = this.type,
            mottakerType = this.mottakerType
        )
    }
}
