package no.nav.historisk.superhelt.brev.db

import jakarta.persistence.*
import no.nav.common.types.EksternJournalpostId
import no.nav.historisk.superhelt.brev.*
import no.nav.historisk.superhelt.sak.db.SakJpaEntity
import org.hibernate.Hibernate
import java.time.Instant

@Entity
@Table(name = "brev")
class BrevJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brev_id")
    var id: Long? = null,

    @Column(name = "brev_uuid")
    val uuid: BrevId,

    val createdDate: Instant = Instant.now(),

    @ManyToOne
    @JoinColumn(name = "sak_id")
    val sak: SakJpaEntity,

    var tittel: String?,

    @Column(columnDefinition = "TEXT")
    var innhold: String?,

    @Enumerated(EnumType.STRING)
    @Column(name = "brev_type")
    val type: BrevType,

    @Enumerated(EnumType.STRING)
    @Column(name = "brev_mottaker_type")
    val mottakerType: BrevMottaker,

    @Enumerated(EnumType.STRING)
    @Column(name = "brev_status")
    var status: BrevStatus = BrevStatus.NY,

    var journalpostId: EksternJournalpostId? = null,

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as BrevJpaEntity

        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    internal fun toDomain(): Brev {
        return Brev(
            saksnummer = sak.saksnummer,
            uuid = this.uuid,
            opprettetTidspunkt = this.createdDate,
            tittel = this.tittel,
            innhold = this.innhold,
            type = this.type,
            mottakerType = this.mottakerType,
            status = this.status,
            journalpostId = this.journalpostId
        )
    }
}
