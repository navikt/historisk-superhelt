package no.nav.historisk.superhelt.endringslogg.db

import jakarta.persistence.*
import no.nav.common.types.NavIdent
import no.nav.historisk.superhelt.endringslogg.EndringsloggLinje
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.sak.db.SakJpaEntity
import org.hibernate.Hibernate
import java.time.Instant

@Entity
@Table(name = "endringslogg")
class EndringsloggJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "endringslogg_id")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sak_id", nullable = false)
    val sak: SakJpaEntity,

    val tidspunkt: Instant,
    val endretAv: NavIdent,
    @Enumerated(EnumType.STRING)
    @Column(name = "endringslogg_type")
    val type: EndringsloggType,
    val endring: String,
    val beskrivelse: String? = null,

    ) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as EndringsloggJpaEntity

        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()


    internal fun toDomain(): EndringsloggLinje {

        return EndringsloggLinje(
            saksnummer = sak.saksnummer,
            endretTidspunkt = tidspunkt,
            type = type,
            endring = endring,
            beskrivelse = beskrivelse,
            endretAv = endretAv,
        )
    }


}
