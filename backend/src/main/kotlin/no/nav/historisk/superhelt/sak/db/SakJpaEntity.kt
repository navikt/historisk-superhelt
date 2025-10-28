package no.nav.historisk.superhelt.sak.db

import jakarta.persistence.*
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.Saksnummer
import no.nav.historisk.superhelt.sak.StonadsType
import no.nav.historisk.superhelt.sak.VedtakType
import no.nav.historisk.superhelt.utbetaling.Utbetaling
import no.nav.person.Fnr
import org.hibernate.Hibernate
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "sak")
//@EntityListeners(AuditingEntityListener::class)
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
    var vedtak: VedtakType? = null,

    var begrunnelse: String? = null,

    var saksbehandler: String,
    var opprettet: LocalDateTime = LocalDateTime.now(),
    var soknadsDato: LocalDate? = null,

    @OneToOne( cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    var utbetaling: UtbetalingJpaEntity? = null,

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
//    var lastModifiedBy: String? = null

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as SakJpaEntity

        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    fun setOrUpdateUtbetaling(utbetalingDomain: Utbetaling?) {
        if (utbetalingDomain == null) {
            this.utbetaling = null
            return
        }
        val entity = this.utbetaling?: UtbetalingJpaEntity(sak= this, belop=0.0)
        entity.belop= utbetalingDomain.belop
        this.utbetaling= entity
    }
}

internal fun SakJpaEntity.toDomain(): Sak {
    return Sak(
        saksnummer = this.id?.let { Saksnummer(it) },
        type = this.type,
        fnr = this.fnr,
        tittel = this.tittel,
        begrunnelse = this.begrunnelse,
        status = this.status,
        vedtak = this.vedtak,
        saksbehandler = this.saksbehandler,
        opprettetDato = this.opprettet.toLocalDate(),
        soknadsDato = this.soknadsDato,
        utbetaling = this.utbetaling.toDomain()
    )
}

internal fun Sak.toEntity(): SakJpaEntity {
    val sakJpaEntity = SakJpaEntity(
        id = this.saksnummer?.id,
        type = this.type,
        fnr = this.fnr,
        tittel = this.tittel,
        begrunnelse = this.begrunnelse,
        status = this.status,
        vedtak = this.vedtak,
        saksbehandler = this.saksbehandler,
        soknadsDato = this.soknadsDato,
    )
    sakJpaEntity.setOrUpdateUtbetaling(this.utbetaling)
    return sakJpaEntity
}
