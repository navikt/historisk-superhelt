package no.nav.historisk.superhelt.sak.db

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.SaksType
import no.nav.person.Fnr
import org.hibernate.Hibernate
import java.time.LocalDateTime

@Entity
@Table(name = "sak")
class SakJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?=null,

    @Enumerated(EnumType.STRING)
    var type: SaksType,
    var fnr: Fnr,
    var tittel: String? = null,

    @Enumerated(EnumType.STRING)
    var status: SakStatus = SakStatus.UNDER_BEHANDLING,

    var begrunnelse: String?= null,

    var saksbehandler: String,
    var opprettet: LocalDateTime = LocalDateTime.now(),
    // Utbetaling
    // Oppgaver
    // Brev
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as SakJpaEntity

        return id != null && id == other.id
    }
    override fun hashCode(): Int = javaClass.hashCode()

}