package no.nav.historisk.superhelt.sak.model

import jakarta.persistence.*
import no.nav.person.Fnr
import org.hibernate.Hibernate

@Entity
@Table(name = "sak")
class SakEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?=null,

    @Enumerated(EnumType.STRING)
    var type: SaksType,
    var person: Fnr,
    var tittel: String? = null,

    @Enumerated(EnumType.STRING)
    var status: SakStatus= SakStatus.UNDER_BEHANDLING,

    // vedtak
    @Enumerated(EnumType.STRING)
    var vedtak: VedtakType? =null,
    var begrunnelse: String?= null,
    // Utbetaling
    // Oppgaver
    // Brev
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as SakEntity

        return id != null && id == other.id
    }
    override fun hashCode(): Int = javaClass.hashCode()

}
