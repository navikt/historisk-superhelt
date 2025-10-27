package no.nav.historisk.superhelt.sak.db

import jakarta.persistence.*
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.StonadsType
import no.nav.historisk.superhelt.sak.VedtakType
import no.nav.person.Fnr
import org.hibernate.Hibernate
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "sak")
class SakJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Enumerated(EnumType.STRING)
    var type: StonadsType,
    var fnr: Fnr,
    var tittel: String? = null,

    @Enumerated(EnumType.STRING)
    var status: SakStatus,

    @Enumerated(EnumType.STRING)
    var vedtak: VedtakType ? = null,

    var begrunnelse: String? = null,

    var saksbehandler: String,
    var opprettet: LocalDateTime = LocalDateTime.now(),
    var soknadsDato: LocalDate? = null,
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